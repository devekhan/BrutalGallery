package com.brutalbeats.gallery.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType

/** On-brand empty state: rotated sticker block, big heading, short message, optional action. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    glyph: Glyph = Glyph.Photos,
    fill: Color = Brutal.colors.accent,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrutalSurface(Modifier.size(96.dp), fill = fill, rotation = -3f) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                BrutalIcon(glyph, size = 48.dp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title.uppercase(),
            style = BrutalType.h2,
            color = Brutal.colors.text,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = BrutalType.body,
            color = Brutal.colors.textMuted,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            BrutalButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** On-brand error state with a retry button. */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        title = "Something broke",
        message = message,
        modifier = modifier,
        glyph = Glyph.Close,
        fill = Brutal.colors.danger,
        actionLabel = if (onRetry != null) "Try again" else null,
        onAction = onRetry
    )
}

/** Three bouncing flat blocks. Cheap to animate, matches the style. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    label: String = "Loading"
) {
    val c = Brutal.colors
    val colors = listOf(c.accent, c.accent2, c.accent3)
    val transition = rememberInfiniteTransition(label = "loading")
    Column(
        modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0 until 3) {
                val h by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(450),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(i * 120)
                    ),
                    label = "bounce$i"
                )
                Box(
                    Modifier
                        .offset { IntOffset(0, (-14.dp * h).roundToPx()) }
                        .size(20.dp)
                        .brutalBox(colors[i], RoundedCornerShape(2.dp), 3.dp, c.ink, 3.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = label.uppercase(),
            style = BrutalType.label,
            color = c.text
        )
    }
}
