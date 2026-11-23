package com.brutalbeats.gallery.ui.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.brutalbeats.gallery.ui.components.BrutalIcon
import com.brutalbeats.gallery.ui.components.GlassPanel
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.components.brutalBox
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType
import com.brutalbeats.gallery.ui.theme.readable

private fun TopTab.glyph(): Glyph = when (this) {
    TopTab.PHOTOS -> Glyph.Photos
    TopTab.ALBUMS -> Glyph.Albums
    TopTab.LIBRARY -> Glyph.Library
}

/** Glass bottom bar. The selected tab is a flat accent block with the brutalist frame. */
@Composable
fun BottomNav(
    current: TopTab,
    onSelect: (TopTab) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier.fillMaxWidth().padding(start = 12.dp, end = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TopTab.values().forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavItem(tab: TopTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Brutal.colors
    val shape = RoundedCornerShape(6.dp)
    val itemModifier = if (selected) {
        Modifier.brutalBox(c.accent, shape, Brutal.dims.border, c.ink, 3.dp)
    } else Modifier
    val fg = if (selected) c.accent.readable() else c.text
    Box(
        modifier
            .heightIn(min = 56.dp)
            .padding(bottom = if (selected) 3.dp else 0.dp, end = if (selected) 3.dp else 0.dp)
            .semantics { this.selected = selected }
            .then(itemModifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClickLabel = "Open ${tab.label}",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrutalIcon(tab.glyph(), tint = fg, size = 24.dp)
            Text(
                text = tab.label.uppercase(),
                style = BrutalType.caption,
                color = fg,
                maxLines = 1
            )
        }
    }
}
