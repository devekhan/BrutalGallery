package com.brutalbeats.gallery.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brutalbeats.gallery.ui.components.BrutalHeading
import com.brutalbeats.gallery.ui.components.BrutalIcon
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.components.Sticker
import com.brutalbeats.gallery.ui.components.brutalBox
import com.brutalbeats.gallery.ui.components.brutalPress
import com.brutalbeats.gallery.ui.nav.LocalBottomInset
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType
import com.brutalbeats.gallery.ui.theme.readable

/** Special places: Favorites, Recently deleted, Hidden, Settings. The first three are built in the next parts. */
@Composable
fun LibraryScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = Brutal.colors
    val bottom = LocalBottomInset.current

    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = bottom + 16.dp)
    ) {
        BrutalHeading(
            "Library",
            style = BrutalType.h1,
            modifier = Modifier.padding(start = 20.dp, end = 24.dp, bottom = 16.dp)
        )
        Column(
            Modifier.padding(start = 20.dp, end = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryCard(
                glyph = Glyph.Heart,
                title = "Favorites",
                subtitle = "Your best shots in one place",
                blockColor = c.accent2,
                soon = true,
                onClick = {}
            )
            LibraryCard(
                glyph = Glyph.Trash,
                title = "Recently deleted",
                subtitle = "Restore anything within 30 days",
                blockColor = c.danger,
                soon = true,
                onClick = {}
            )
            LibraryCard(
                glyph = Glyph.Lock,
                title = "Hidden",
                subtitle = "Locked with your fingerprint or PIN",
                blockColor = c.accent4,
                soon = true,
                onClick = {}
            )
            LibraryCard(
                glyph = Glyph.Settings,
                title = "Settings",
                subtitle = "Theme, accent color, glass, grid size",
                blockColor = c.accent3,
                soon = false,
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun LibraryCard(
    glyph: Glyph,
    title: String,
    subtitle: String,
    blockColor: Color,
    soon: Boolean,
    onClick: () -> Unit
) {
    val c = Brutal.colors
    val shape = RoundedCornerShape(Brutal.dims.corner)
    Box(Modifier.fillMaxWidth().padding(end = 6.dp, bottom = 6.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .brutalPress(onClick, shape, 5.dp, c.ink)
                .clip(shape)
                .background(c.surface)
                .border(Brutal.dims.border, c.ink, shape)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .brutalBox(blockColor, RoundedCornerShape(6.dp), Brutal.dims.border, c.ink, 3.dp),
                contentAlignment = Alignment.Center
            ) {
                BrutalIcon(glyph, tint = blockColor.readable(), size = 28.dp)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    style = BrutalType.h3,
                    color = c.surface.readable()
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = BrutalType.caption,
                    color = c.surface.readable().copy(alpha = 0.7f)
                )
            }
            if (soon) Sticker("Soon", fill = c.accent, rotation = 3f)
        }
    }
}
