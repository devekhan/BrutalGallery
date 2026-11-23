package com.brutalbeats.gallery.ui.screens.photos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brutalbeats.gallery.data.MediaFilter
import com.brutalbeats.gallery.ui.components.BrutalButton
import com.brutalbeats.gallery.ui.components.BrutalChip
import com.brutalbeats.gallery.ui.components.BrutalHeading
import com.brutalbeats.gallery.ui.components.BrutalIcon
import com.brutalbeats.gallery.ui.components.BrutalIconButton
import com.brutalbeats.gallery.ui.components.BrutalSurface
import com.brutalbeats.gallery.ui.components.EmptyState
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.components.LoadingState
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType
import com.brutalbeats.gallery.ui.theme.readable

/** The main timeline of every photo and video, newest first. */
@Composable
fun PhotosScreen(
    partialAccess: Boolean,
    onManageAccess: () -> Unit,
    modifier: Modifier = Modifier,
    vm: PhotosViewModel = hiltViewModel()
) {
    val c = Brutal.colors
    val context = LocalContext.current
    val entries by vm.entries.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle()
    val columns by vm.columns.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val list = entries

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 24.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                BrutalHeading("Photos", style = BrutalType.h1)
                Text("$total ITEMS", style = BrutalType.label, color = c.textMuted)
            }
            BrutalIconButton(
                onClick = vm::refresh,
                contentDescription = "Refresh",
                fill = c.surface
            ) { BrutalIcon(Glyph.Refresh) }
        }

        if (partialAccess) {
            BrutalSurface(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 22.dp, bottom = 12.dp),
                fill = c.accent2
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "YOU ONLY SHARED SOME PHOTOS",
                        style = BrutalType.h3,
                        color = c.accent2.readable()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Allow access to all photos and videos in Settings to see your whole library.",
                        style = BrutalType.body,
                        color = c.accent2.readable()
                    )
                    Spacer(Modifier.height(10.dp))
                    BrutalButton(text = "Open settings", onClick = onManageAccess, fill = c.surface)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MediaFilter.values().forEach { f ->
                BrutalChip(text = f.label, selected = f == filter, onClick = { vm.setFilter(f) })
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingState()
                }
                list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = if (filter == MediaFilter.ALL) "No photos yet" else "Nothing here",
                        message = if (filter == MediaFilter.ALL) {
                            "Take a photo or copy some to your phone and it shows up here."
                        } else {
                            "No ${filter.label.lowercase()} to show."
                        },
                        glyph = Glyph.Photos
                    )
                }
                else -> TimelineGrid(
                    entries = list,
                    columns = columns,
                    onColumnsChange = vm::setColumns,
                    onOpen = { openExternally(context, it) }
                )
            }
        }
    }
}
