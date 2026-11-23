package com.brutalbeats.gallery.ui.screens.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brutalbeats.gallery.data.GalleryItem
import com.brutalbeats.gallery.data.TimelineEntry
import com.brutalbeats.gallery.ui.components.BrutalHeading
import com.brutalbeats.gallery.ui.components.BrutalIcon
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.nav.LocalBottomInset
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType

/** 3:45 or 1:02:09. */
fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/** Opens a photo or video in another app for now. The built-in viewer arrives in Part 2. */
fun openExternally(context: Context, item: GalleryItem) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(Uri.parse(item.uri), item.mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        // No app can open it: nothing to do.
    }
}

/** Two fingers apart = bigger photos (fewer columns). Together = smaller photos. Single-finger scrolling is untouched. */
private fun Modifier.pinchToChangeColumns(columns: Int, onColumns: (Int) -> Unit): Modifier =
    pointerInput(columns) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var zoomTotal = 1f
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.size >= 2) {
                    zoomTotal *= event.calculateZoom()
                    event.changes.forEach { it.consume() }
                    if (zoomTotal > 1.3f) {
                        onColumns((columns - 1).coerceAtLeast(2))
                        return@awaitEachGesture
                    }
                    if (zoomTotal < 0.75f) {
                        onColumns((columns + 1).coerceAtMost(6))
                        return@awaitEachGesture
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }

/** The photo grid: date headings with square thumbnails. Used by Photos and by every album. */
@Composable
fun TimelineGrid(
    entries: List<TimelineEntry>,
    columns: Int,
    onColumnsChange: (Int) -> Unit,
    onOpen: (GalleryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottom = LocalBottomInset.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize().pinchToChangeColumns(columns, onColumnsChange),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 4.dp, bottom = bottom + 16.dp)
    ) {
        items(
            items = entries,
            key = { it.key },
            span = { entry ->
                if (entry is TimelineEntry.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1)
            },
            contentType = { if (it is TimelineEntry.Header) 0 else 1 }
        ) { entry ->
            when (entry) {
                is TimelineEntry.Header -> DateHeader(entry)
                is TimelineEntry.Media -> MediaThumb(
                    item = entry.item,
                    columns = columns,
                    onClick = { onOpen(entry.item) }
                )
            }
        }
    }
}

@Composable
private fun DateHeader(header: TimelineEntry.Header) {
    val c = Brutal.colors
    Row(
        Modifier.fillMaxWidth().padding(start = 3.dp, top = 16.dp, bottom = 6.dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BrutalHeading(header.label, style = BrutalType.h3, modifier = Modifier.weight(1f), maxLines = 1)
        Text("${header.count}", style = BrutalType.label, color = c.textMuted)
    }
}

/** One square thumbnail with a thick border. Videos get a duration badge. */
@Composable
fun MediaThumb(
    item: GalleryItem,
    columns: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = Brutal.colors
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sizePx = with(density) { (screenWidth / columns).roundToPx() }
    val request = remember(item.uri, sizePx) {
        ImageRequest.Builder(context).data(item.uri).size(sizePx).build()
    }
    val borderWidth = if (columns <= 3) 3.dp else 2.dp
    val description = if (item.isVideo) "Video, ${formatDuration(item.durationMs)}" else "Photo"

    Box(
        modifier
            .padding(3.dp)
            .aspectRatio(1f)
            .semantics { this.contentDescription = description }
            .background(c.surface)
            .border(borderWidth, c.ink)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Open",
                onClick = onClick
            )
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().padding(borderWidth)
        )
        if (item.isVideo) {
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(borderWidth)
                    .background(c.ink)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                BrutalIcon(Glyph.Play, tint = Color.White, size = 10.dp)
                if (columns <= 4) {
                    Text(
                        formatDuration(item.durationMs),
                        style = BrutalType.caption,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
