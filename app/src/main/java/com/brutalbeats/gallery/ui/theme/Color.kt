package com.brutalbeats.gallery.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Raw palette: flat, saturated, high contrast. No gradients on flat color blocks. */
object Palette {
    val Ink = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)

    val Yellow = Color(0xFFFFD93D)
    val HotPink = Color(0xFFFF4D8D)
    val Mint = Color(0xFF3DDC97)
    val ElectricBlue = Color(0xFF2F6BFF)
    val Orange = Color(0xFFFF8A1F)
    val Red = Color(0xFFFF3B30)

    val PaperLight = Color(0xFFFFF7E0)
    val PaperDark = Color(0xFF2A2A33)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF3B3B47)

    /** Accent swatches offered in Settings. */
    val AccentPresets = listOf(Yellow, HotPink, Mint, ElectricBlue, Orange)
}

/** Black text on light fills, white text on dark fills. */
fun Color.readable(): Color = if (luminance() > 0.40f) Color.Black else Color.White
