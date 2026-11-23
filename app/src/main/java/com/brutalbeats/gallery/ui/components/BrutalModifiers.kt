package com.brutalbeats.gallery.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp

/**
 * Hard offset shadow: a solid copy of [shape] drawn [offset] right and down, ZERO blur.
 * [collapse] 0 = full shadow, 1 = shadow fully collapsed (used by the press animation).
 * The shadow is drawn outside the element's bounds, so leave padding around it.
 */
fun Modifier.hardShadow(
    offset: Dp,
    color: Color,
    shape: Shape = RectangleShape,
    collapse: () -> Float = { 0f }
): Modifier = this.drawBehind {
    val o = offset.toPx() * (1f - collapse())
    if (o > 0.01f) {
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply { addOutline(outline) }
        translate(o, o) { drawPath(path, color) }
    }
}

/** The static brutalist box: hard shadow, flat fill, thick border. */
fun Modifier.brutalBox(
    fill: Color,
    shape: Shape,
    borderWidth: Dp,
    borderColor: Color,
    shadow: Dp,
    shadowColor: Color = borderColor
): Modifier = this
    .hardShadow(shadow, shadowColor, shape)
    .clip(shape)
    .background(fill)
    .border(borderWidth, borderColor, shape)

/**
 * Press-down behavior: on touch the element slides into its shadow while the shadow collapses,
 * with a springy release. Adds click handling and haptics. Put clip/background/border AFTER this.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.brutalPress(
    onClick: () -> Unit,
    shape: Shape,
    shadow: Dp,
    shadowColor: Color,
    enabled: Boolean = true,
    role: Role = Role.Button,
    hapticStrong: Boolean = false,
    onLongClick: (() -> Unit)? = null
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val progress by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "brutalPress"
    )
    val haptics = LocalHapticFeedback.current
    return this
        .graphicsLayer {
            val t = shadow.toPx() * progress
            translationX = t
            translationY = t
        }
        .hardShadow(shadow, shadowColor, shape) { progress }
        .combinedClickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onLongClick = onLongClick,
            onClick = {
                haptics.performHapticFeedback(
                    if (hapticStrong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                )
                onClick()
            }
        )
}
