package com.brutalbeats.gallery.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brutalbeats.gallery.data.ThemeMode
import com.brutalbeats.gallery.permissions.MediaAccess
import com.brutalbeats.gallery.permissions.findActivity
import com.brutalbeats.gallery.permissions.rememberMediaPermissions
import com.brutalbeats.gallery.ui.components.EmptyState
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.components.LocalGlassState
import com.brutalbeats.gallery.ui.components.glassSource
import com.brutalbeats.gallery.ui.components.rememberGlassState
import com.brutalbeats.gallery.ui.nav.BottomNav
import com.brutalbeats.gallery.ui.nav.LocalBottomInset
import com.brutalbeats.gallery.ui.nav.Navigator
import com.brutalbeats.gallery.ui.nav.Page
import com.brutalbeats.gallery.ui.nav.TopTab
import com.brutalbeats.gallery.ui.screens.albums.AlbumDetailScreen
import com.brutalbeats.gallery.ui.screens.albums.AlbumsScreen
import com.brutalbeats.gallery.ui.screens.library.LibraryScreen
import com.brutalbeats.gallery.ui.screens.photos.PhotosScreen
import com.brutalbeats.gallery.ui.screens.settings.SettingsScreen
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalTheme
import com.brutalbeats.gallery.ui.theme.LocalGlassIntensity

/** Picks the theme from your settings, then draws the app. */
@Composable
fun BrutalGalleryApp(vm: AppViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val s = settings
    if (s == null) {
        Box(Modifier.fillMaxSize()) // one frame while settings load
    } else {
        val systemDark = isSystemInDarkTheme()
        val dark = when (s.themeMode) {
            ThemeMode.SYSTEM -> systemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        BrutalTheme(
            darkTheme = dark,
            highContrast = s.highContrast,
            accent = Color(s.accentArgb),
            glassIntensity = s.glassIntensity
        ) {
            val view = LocalView.current
            SideEffect {
                val window = view.context.findActivity()?.window
                if (window != null) {
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !dark
                    controller.isAppearanceLightNavigationBars = !dark
                }
            }
            AppRoot(vm)
        }
    }
}

@Composable
private fun AppRoot(vm: AppViewModel) {
    val c = Brutal.colors
    val perms = rememberMediaPermissions()
    val navigator = remember { Navigator() }
    val glass = rememberGlassState()
    val intensity = LocalGlassIntensity.current

    // Ask for photo access on launch; read the library as soon as we have any.
    LaunchedEffect(Unit) {
        if (perms.access == MediaAccess.NONE) perms.request()
    }
    LaunchedEffect(perms.access) {
        if (perms.access != MediaAccess.NONE) vm.startLibrary()
    }

    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomInset = navBarInset + 86.dp

    // Back: close a page, else go to Photos.
    BackHandler(enabled = navigator.stack.isNotEmpty() || navigator.tab != TopTab.PHOTOS) {
        if (!navigator.pop()) navigator.select(TopTab.PHOTOS)
    }

    CompositionLocalProvider(
        LocalGlassState provides glass,
        LocalBottomInset provides bottomInset
    ) {
        Box(Modifier.fillMaxSize().background(c.background)) {
            // The screens. This box is what the glass bottom bar frosts.
            Box(Modifier.fillMaxSize().glassSource(glass, intensity)) {
                if (perms.access == MediaAccess.NONE) {
                    Box(
                        Modifier.fillMaxSize().statusBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "Let me see your photos",
                            message = if (perms.blocked) {
                                "Android won't ask again. Open Settings, then Permissions, and allow photos and videos."
                            } else {
                                "Brutal Gallery needs to read the photos and videos on your phone. " +
                                    "Nothing leaves your device: this app has no internet access at all."
                            },
                            glyph = Glyph.Photos,
                            actionLabel = if (perms.blocked) "Open settings" else "Give access",
                            onAction = if (perms.blocked) perms::openSettings else perms::request
                        )
                    }
                } else {
                    PageHost(navigator, partial = perms.access == MediaAccess.PARTIAL, onManageAccess = perms::openSettings)
                }
            }

            if (perms.access != MediaAccess.NONE) {
                Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                    BottomNav(current = navigator.tab, onSelect = { navigator.select(it) })
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun PageHost(navigator: Navigator, partial: Boolean, onManageAccess: () -> Unit) {
    val current: Any = navigator.stack.lastOrNull() ?: navigator.tab
    AnimatedContent(
        targetState = current,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "pages"
    ) { target ->
        when (target) {
            is TopTab -> when (target) {
                TopTab.PHOTOS -> PhotosScreen(partialAccess = partial, onManageAccess = onManageAccess)
                TopTab.ALBUMS -> AlbumsScreen(onOpenAlbum = { navigator.push(Page.Album(it.bucketId, it.name)) })
                TopTab.LIBRARY -> LibraryScreen(onOpenSettings = { navigator.push(Page.Settings) })
            }
            is Page.Album -> AlbumDetailScreen(
                bucketId = target.bucketId,
                name = target.name,
                onBack = { navigator.pop() }
            )
            is Page.Settings -> SettingsScreen(onBack = { navigator.pop() })
            else -> Unit
        }
    }
}
