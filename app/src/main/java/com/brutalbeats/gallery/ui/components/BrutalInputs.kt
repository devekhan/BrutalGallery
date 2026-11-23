package com.brutalbeats.gallery.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType
import com.brutalbeats.gallery.ui.theme.Palette
import com.brutalbeats.gallery.ui.theme.readable

/** Text input: thick border, hard shadow, flat fill. Leave padding for the shadow. */
@Composable
fun BrutalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val c = Brutal.colors
    val shape = RoundedCornerShape(Brutal.dims.corner)
    val textColor = c.surface.readable()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        textStyle = BrutalType.bodyBold.copy(color = textColor),
        cursorBrush = SolidColor(textColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .brutalBox(c.surface, shape, Brutal.dims.border, c.ink, 4.dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                leading?.invoke()
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = BrutalType.body,
                            color = textColor.copy(alpha = 0.55f),
                            maxLines = 1
                        )
                    }
                    inner()
                }
            }
        }
    )
}

/** Square-thumb switch with a springy slide. 48dp tall touch target. */
@Composable
fun BrutalSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val c = Brutal.colors
    val haptics = LocalHapticFeedback.current
    val pos by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "switch"
    )
    val trackFill = if (checked) c.accent3 else c.surface
    Box(
        modifier
            .size(width = 72.dp, height = 48.dp)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(it)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(width = 60.dp, height = 34.dp)
                .brutalBox(trackFill, RoundedCornerShape(6.dp), 3.dp, c.ink, 3.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset((4.dp + 30.dp * pos).roundToPx(), 0) }
                    .size(22.dp)
                    .brutalBox(Palette.White, RoundedCornerShape(3.dp), 3.dp, c.ink, 0.dp)
            )
        }
    }
}

/** Thick horizontal slider with a square thumb. Used for seek bar, glass intensity, crossfade... */
@Composable
fun BrutalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    fill: Color = Brutal.colors.accent,
    enabled: Boolean = true
) {
    val c = Brutal.colors
    val borderDp = Brutal.dims.border
    val onChange by rememberUpdatedState(onValueChange)
    val onFinished by rememberUpdatedState(onValueChangeFinished)
    val start = valueRange.start
    val span = valueRange.endInclusive - valueRange.start
    val frac = if (span <= 0f) 0f else ((value - start) / span).coerceIn(0f, 1f)
    val trackColor = c.surface
    val ink = c.ink

    Canvas(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    value.coerceIn(valueRange.start, valueRange.endInclusive),
                    valueRange
                )
                setProgress { target ->
                    onChange(target.coerceIn(valueRange.start, valueRange.endInclusive))
                    true
                }
            }
            .pointerInput(enabled, start, span) {
                if (!enabled) return@pointerInput
                val thumbPx = 20.dp.toPx()
                fun toValue(x: Float): Float {
                    val usable = (size.width - thumbPx).coerceAtLeast(1f)
                    val f = ((x - thumbPx / 2f) / usable).coerceIn(0f, 1f)
                    return start + f * span
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    onChange(toValue(down.position.x))
                    drag(down.id) { change ->
                        change.consume()
                        onChange(toValue(change.position.x))
                    }
                    onFinished?.invoke()
                }
            }
    ) {
        val border = borderDp.toPx()
        val trackH = 16.dp.toPx()
        val top = (size.height - trackH) / 2f
        val thumbW = 20.dp.toPx()
        val thumbH = 32.dp.toPx()
        val thumbTop = (size.height - thumbH) / 2f
        val usable = (size.width - thumbW).coerceAtLeast(1f)
        val cx = thumbW / 2f + usable * frac
        val shadow = 3.dp.toPx()

        // track + progress + outline
        drawRect(trackColor, Offset(0f, top), Size(size.width, trackH))
        drawRect(fill, Offset(0f, top), Size(cx, trackH))
        drawRect(ink, Offset(0f, top), Size(size.width, trackH), style = Stroke(border))

        // thumb: hard shadow, flat fill, outline
        val left = cx - thumbW / 2f
        drawRect(ink, Offset(left + shadow, thumbTop + shadow), Size(thumbW, thumbH))
        drawRect(Palette.White, Offset(left, thumbTop), Size(thumbW, thumbH))
        drawRect(ink, Offset(left, thumbTop), Size(thumbW, thumbH), style = Stroke(border))
    }
}
