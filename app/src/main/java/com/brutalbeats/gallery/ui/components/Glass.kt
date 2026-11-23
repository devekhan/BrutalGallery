package com.brutalbeats.gallery.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.LocalGlassEnabled
import com.brutalbeats.gallery.ui.theme.LocalGlassIntensity

/**
 * Backdrop-blur engine (no extra library).
 * The screen's content is recorded into one GraphicsLayer that carries a blur RenderEffect;
 * each GlassPanel then draws "its window" of that blurred layer behind itself.
 * Real blur on Android 12+ (API 31). Older versions get a tinted translucent fallback.
 */
@Stable
class GlassState internal constructor(internal val layer: GraphicsLayer) {
    internal var sourceOrigin by mutableStateOf(Offset.Zero)
}

@Composable
fun rememberGlassState(): GlassState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassState(layer) }
}

/** Provide the screen's GlassState so any GlassPanel below finds it automatically. */
val LocalGlassState = staticCompositionLocalOf<GlassState?> { null }

/**
 * Mark the content that should appear blurred behind glass panels (background art, lists).
 * Panels must be SIBLINGS drawn on top of this node, not children of it.
 * [intensity] 0..1 sets how strong the blur is.
 */
fun Modifier.glassSource(state: GlassState, intensity: Float): Modifier = this
    .onGloballyPositioned { state.sourceOrigin = it.positionInRoot() }
    .drawWithContent {
        if (Build.VERSION.SDK_INT >= 31) {
            val radius = (6f + 22f * intensity.coerceIn(0f, 1f)).dp.toPx()
            state.layer.renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
            state.layer.record {
                this@drawWithContent.drawContent()
            }
        }
        drawContent()
    }

@Composable
private fun Modifier.glassBackdrop(state: GlassState?, shape: Shape): Modifier {
    var origin by remember { mutableStateOf(Offset.Zero) }
    if (state == null || Build.VERSION.SDK_INT < 31) return this
    return this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)
            val path = Path().apply { addOutline(outline) }
            clipPath(path) {
                val d = state.sourceOrigin - origin
                translate(d.x, d.y) { drawLayer(state.layer) }
            }
        }
}

/**
 * Liquid-glass panel inside the brutalist frame:
 * hard black shadow + thick black border (the frame), frosted blurred fill with sheen and a
 * thin specular edge light (the glass).
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Brutal.dims.corner),
    intensity: Float = LocalGlassIntensity.current,
    shadow: Dp = Brutal.dims.shadow,
    rotation: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val c = Brutal.colors
    val borderWidth = Brutal.dims.border
    val glassOn = LocalGlassEnabled.current
    val state = LocalGlassState.current
    val i = intensity.coerceIn(0f, 1f)

    val tintAlpha = if (c.isDark) 0.40f + 0.35f * i else 0.30f + 0.45f * i
    val tint = if (glassOn) c.glassTint.copy(alpha = tintAlpha) else c.surface
    val sheen = remember {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent))
    }
    val specular = remember {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.05f)))
    }

    Box(
        modifier
            .graphicsLayer { rotationZ = rotation }
            .hardShadow(shadow, c.ink, shape)
            .clip(shape)
            .glassBackdrop(if (glassOn) state else null, shape)
            .background(tint)
            .drawBehind {
                if (glassOn) {
                    drawRect(sheen, size = Size(size.width, size.height * 0.5f))
                    val inset = borderWidth.toPx() + 1.dp.toPx()
                    drawRect(
                        brush = specular,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .border(borderWidth, c.ink, shape)
    ) {
        CompositionLocalProvider(LocalContentColor provides c.text) { content() }
    }
}
