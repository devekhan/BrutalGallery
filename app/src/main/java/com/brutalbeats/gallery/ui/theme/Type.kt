package com.brutalbeats.gallery.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.brutalbeats.gallery.R

// Fonts are bundled (downloaded into res/font by the GitHub workflow). No runtime downloads.
// Pairing note: Archivo Black (chunky headlines) + Space Mono (technical metadata) is the
// pairing from your brief and it suits the look, so I kept it.
val HeadingFont = FontFamily(Font(R.font.archivo_black, FontWeight.Normal))

val BodyFont = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold)
)

/** All text styles used in the app. Headings are shown uppercase by BrutalHeading. */
object BrutalType {
    val display = TextStyle(fontFamily = HeadingFont, fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1).sp)
    val h1 = TextStyle(fontFamily = HeadingFont, fontSize = 30.sp, lineHeight = 33.sp, letterSpacing = (-0.5).sp)
    val h2 = TextStyle(fontFamily = HeadingFont, fontSize = 22.sp, lineHeight = 26.sp)
    val h3 = TextStyle(fontFamily = HeadingFont, fontSize = 16.sp, lineHeight = 20.sp)
    val body = TextStyle(fontFamily = BodyFont, fontSize = 14.sp, lineHeight = 20.sp)
    val bodyBold = body.copy(fontWeight = FontWeight.Bold)
    val label = TextStyle(fontFamily = BodyFont, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    val caption = TextStyle(fontFamily = BodyFont, fontSize = 11.sp, lineHeight = 14.sp)
}

/** Maps our fonts onto Material so any base Material3 piece we touch still uses them. */
val BrutalMaterialTypography = Typography(
    displayLarge = BrutalType.display,
    displayMedium = BrutalType.h1,
    displaySmall = BrutalType.h2,
    headlineLarge = BrutalType.h1,
    headlineMedium = BrutalType.h2,
    headlineSmall = BrutalType.h3,
    titleLarge = BrutalType.h2,
    titleMedium = BrutalType.h3,
    titleSmall = BrutalType.bodyBold,
    bodyLarge = BrutalType.body,
    bodyMedium = BrutalType.body,
    bodySmall = BrutalType.caption,
    labelLarge = BrutalType.label,
    labelMedium = BrutalType.label,
    labelSmall = BrutalType.caption
)
