package com.brutalbeats.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType
import com.brutalbeats.gallery.ui.theme.readable

/** Oversized, bold, uppercase heading. */
@Composable
fun BrutalHeading(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = BrutalType.h1,
    color: Color = Brutal.colors.text,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/** Flat card: fill + thick border + hard shadow. Leave padding around it for the shadow. */
@Composable
fun BrutalSurface(
    modifier: Modifier = Modifier,
    fill: Color = Brutal.colors.surface,
    shape: Shape = RoundedCornerShape(Brutal.dims.corner),
    shadow: Dp = Brutal.dims.shadow,
    rotation: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    val contentColor = fill.readable()
    Box(
        modifier
            .graphicsLayer { rotationZ = rotation }
            .brutalBox(fill, shape, Brutal.dims.border, Brutal.colors.ink, shadow)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
    }
}

/** Chunky text button with the press-into-shadow animation. */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Color = Brutal.colors.accent,
    enabled: Boolean = true,
    shadow: Dp = Brutal.dims.shadow,
    shape: Shape = RoundedCornerShape(Brutal.dims.corner),
    leading: (@Composable () -> Unit)? = null
) {
    val c = Brutal.colors
    val bg = if (enabled) fill else Color(0xFFB8B8B8)
    val contentColor = bg.readable()
    Row(
        modifier
            .heightIn(min = 48.dp)
            .brutalPress(onClick, shape, shadow, c.ink, enabled = enabled)
            .clip(shape)
            .background(bg)
            .border(Brutal.dims.border, c.ink, shape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            leading?.invoke()
            Text(
                text = text.uppercase(),
                style = BrutalType.label.copy(fontSize = 14.sp),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Square (or round, for play buttons) icon button, at least 48dp.
 * Put a custom-drawn icon in [content]. [contentDescription] is required for accessibility.
 */
@Composable
fun BrutalIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fill: Color = Brutal.colors.accent,
    round: Boolean = false,
    size: Dp = 52.dp,
    shadow: Dp = 4.dp,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val c = Brutal.colors
    val shape: Shape = if (round) CircleShape else RoundedCornerShape(Brutal.dims.corner)
    val contentColor = fill.readable()
    Box(
        modifier
            .size(size.coerceAtLeast(48.dp))
            .semantics { this.contentDescription = contentDescription }
            .brutalPress(onClick, shape, shadow, c.ink, enabled = enabled, hapticStrong = round, onLongClick = onLongClick)
            .clip(shape)
            .background(fill)
            .border(Brutal.dims.border, c.ink, shape),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
    }
}

/** Selectable filter chip (tabs, sort options, presets). */
@Composable
fun BrutalChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedFill: Color = Brutal.colors.accent
) {
    val c = Brutal.colors
    val shape = RoundedCornerShape(Brutal.dims.corner)
    val bg = if (selected) selectedFill else c.surface
    val contentColor = bg.readable()
    Box(
        modifier
            .heightIn(min = 48.dp)
            .semantics { this.selected = selected }
            .brutalPress(onClick, shape, 3.dp, c.ink)
            .clip(shape)
            .background(bg)
            .border(Brutal.dims.border, c.ink, shape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = BrutalType.label,
            color = contentColor,
            maxLines = 1
        )
    }
}

/** Small rotated tag, e.g. the "CONNECTED" sticker. Rotation range is -3..+3 degrees. */
@Composable
fun Sticker(
    text: String,
    modifier: Modifier = Modifier,
    fill: Color = Brutal.colors.accent2,
    rotation: Float = -3f
) {
    Box(
        modifier
            .graphicsLayer { rotationZ = rotation.coerceIn(-3f, 3f) }
            .brutalBox(fill, RoundedCornerShape(4.dp), 3.dp, Brutal.colors.ink, 3.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = BrutalType.label.copy(fontSize = 11.sp),
            color = fill.readable(),
            maxLines = 1
        )
    }
}
