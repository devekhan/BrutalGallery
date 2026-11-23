package com.brutalbeats.gallery.ui.screens.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.brutalbeats.gallery.data.AlbumInfo
import com.brutalbeats.gallery.ui.components.BrutalHeading
import com.brutalbeats.gallery.ui.components.BrutalIcon
import com.brutalbeats.gallery.ui.components.BrutalIconButton
import com.brutalbeats.gallery.ui.components.EmptyState
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.components.LoadingState
import com.brutalbeats.gallery.ui.components.brutalBox
import com.brutalbeats.gallery.ui.nav.LocalBottomInset
import com.brutalbeats.gallery.ui.screens.photos.TimelineGrid
import com.brutalbeats.gallery.ui.screens.photos.openExternally
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType

/** Every folder on the phone as a card with its newest photo as the cover. */
@Composable
fun AlbumsScreen(
    onOpenAlbum: (AlbumInfo) -> Unit,
    modifier: Modifier = Modifier,
    vm: AlbumsViewModel = hiltViewModel()
) {
    val c = Brutal.colors
    val bottom = LocalBottomInset.current
    val albums by vm.albums.collectAsStateWithLifecycle()
    val list = albums

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.padding(start = 20.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)) {
            BrutalHeading("Folders", style = BrutalType.h1)
            Text(
                "${list?.size ?: 0} FOLDERS",
                style = BrutalType.label,
                color = c.textMuted
            )
        }
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingState() }
            list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState("No folders", "Folders appear once you have photos or videos.", glyph = Glyph.Folder)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = bottom + 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(list, key = { it.bucketId }) { album ->
                    AlbumCard(album = album, onClick = { onOpenAlbum(album) })
                }
            }
        }
    }
}

@Composable
private fun AlbumCard(album: AlbumInfo, onClick: () -> Unit) {
    val c = Brutal.colors
    val context = LocalContext.current
    val shape = RoundedCornerShape(Brutal.dims.corner)
    val request = remember(album.cover.uri) {
        ImageRequest.Builder(context).data(album.cover.uri).size(360).build()
    }
    Column(
        Modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Open folder ${album.name}",
                onClick = onClick
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .brutalBox(c.surface, shape, Brutal.dims.border, c.ink, 5.dp)
        ) {
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = album.name,
            style = BrutalType.bodyBold,
            color = c.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (album.videoCount > 0) "${album.count} items · ${album.videoCount} videos" else "${album.count} items",
            style = BrutalType.caption,
            color = c.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The photos of one folder. */
@Composable
fun AlbumDetailScreen(
    bucketId: Long,
    name: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: AlbumsViewModel = hiltViewModel()
) {
    val c = Brutal.colors
    val context = LocalContext.current
    val flow = remember(bucketId) { vm.albumEntries(bucketId) }
    val entries by flow.collectAsStateWithLifecycle(initialValue = null)
    val columns by vm.columns.collectAsStateWithLifecycle()
    val list = entries

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 24.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BrutalIconButton(onClick = onBack, contentDescription = "Go back", fill = c.surface) {
                BrutalIcon(Glyph.Back)
            }
            BrutalHeading(name, style = BrutalType.h2, modifier = Modifier.weight(1f), maxLines = 2)
        }
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingState() }
            list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState("Empty folder", "There is nothing in this folder any more.", glyph = Glyph.Folder)
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
