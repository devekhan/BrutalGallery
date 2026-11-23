package com.brutalbeats.gallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Every color the app uses. `ink` = borders and hard shadows. */
@Immutable
data class BrutalColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val ink: Color,
    val text: Color,
    val textMuted: Color,
    val accent: Color,
    val accent2: Color,
    val accent3: Color,
    val accent4: Color,
    val danger: Color,
    val glassTint: Color
)

/** Structure sizes. Border 3-4dp, hard shadow with zero blur. */
@Immutable
data class BrutalDims(val border: Dp, val shadow: Dp, val corner: Dp)

val LocalBrutalColors = staticCompositionLocalOf<BrutalColors> {
    error("Wrap your UI in BrutalTheme { }")
}
val LocalBrutalDims = staticCompositionLocalOf { BrutalDims(3.dp, 6.dp, 8.dp) }

/** 0f..1f: how frosted the glass panels are. */
val LocalGlassIntensity = compositionLocalOf { 0.6f }

/** False in high-contrast mode: glass becomes a solid panel. */
val LocalGlassEnabled = staticCompositionLocalOf { true }

fun brutalColors(dark: Boolean, highContrast: Boolean, accent: Color): BrutalColors {
    val hcDark = highContrast && dark
    return BrutalColors(
        isDark = dark,
        background = when {
            hcDark -> Color.Black
            highContrast -> Color.White
            dark -> Palette.PaperDark
            else -> Palette.PaperLight
        },
        surface = when {
            hcDark -> Color(0xFF1A1A1A)
            highContrast -> Color.White
            dark -> Palette.SurfaceDark
            else -> Palette.SurfaceLight
        },
        ink = if (hcDark) Color.White else Color.Black,
        text = if (dark) Color.White else Color.Black,
        textMuted = when {
            hcDark -> Color(0xFFE6E6E6)
            highContrast -> Color(0xFF1A1A1A)
            dark -> Color(0xFFC9C9D4)
            else -> Color(0xFF4A4A4A)
        },
        accent = accent,
        accent2 = Palette.HotPink,
        accent3 = Palette.Mint,
        accent4 = Palette.ElectricBlue,
        danger = Palette.Red,
        glassTint = if (dark) Color(0xFF14141A) else Color.White
    )
}

/** Short accessors: Brutal.colors.accent, Brutal.dims.border */
object Brutal {
    val colors: BrutalColors
        @Composable @ReadOnlyComposable get() = LocalBrutalColors.current
    val dims: BrutalDims
        @Composable @ReadOnlyComposable get() = LocalBrutalDims.current
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrutalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    accent: Color = Palette.Yellow,
    glassIntensity: Float = 0.6f,
    content: @Composable () -> Unit
) {
    val colors = remember(darkTheme, highContrast, accent) {
        brutalColors(darkTheme, highContrast, accent)
    }
    val dims = remember(highContrast) {
        if (highContrast) BrutalDims(border = 4.dp, shadow = 8.dp, corner = 4.dp)
        else BrutalDims(border = 3.dp, shadow = 6.dp, corner = 8.dp)
    }
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent, onPrimary = Color.Black,
            secondary = colors.accent2, onSecondary = Color.Black,
            background = colors.background, onBackground = colors.text,
            surface = colors.surface, onSurface = colors.text,
            error = colors.danger
        )
    } else {
        lightColorScheme(
            primary = colors.accent, onPrimary = Color.Black,
            secondary = colors.accent2, onSecondary = Color.Black,
            background = colors.background, onBackground = colors.text,
            surface = colors.surface, onSurface = colors.text,
            error = colors.danger
        )
    }

    CompositionLocalProvider(
        LocalBrutalColors provides colors,
        LocalBrutalDims provides dims,
        LocalGlassIntensity provides glassIntensity,
        LocalGlassEnabled provides !highContrast,
        LocalRippleConfiguration provides null // no default Material ripples anywhere
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = BrutalMaterialTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(2.dp),
                small = RoundedCornerShape(4.dp),
                medium = RoundedCornerShape(8.dp),
                large = RoundedCornerShape(12.dp),
                extraLarge = RoundedCornerShape(12.dp)
            ),
            content = content
        )
    }
}
