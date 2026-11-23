package com.brutalbeats.gallery.ui.screens.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brutalbeats.gallery.data.ThemeMode
import com.brutalbeats.gallery.ui.components.BrutalChip
import com.brutalbeats.gallery.ui.components.BrutalHeading
import com.brutalbeats.gallery.ui.components.BrutalIcon
import com.brutalbeats.gallery.ui.components.BrutalIconButton
import com.brutalbeats.gallery.ui.components.BrutalSlider
import com.brutalbeats.gallery.ui.components.BrutalSwitch
import com.brutalbeats.gallery.ui.components.Glyph
import com.brutalbeats.gallery.ui.nav.LocalBottomInset
import com.brutalbeats.gallery.ui.theme.Brutal
import com.brutalbeats.gallery.ui.theme.BrutalType
import com.brutalbeats.gallery.ui.theme.Palette
import kotlin.math.roundToInt

private val ACCENT_NAMES = listOf("Yellow", "Hot pink", "Mint", "Electric blue", "Orange")

/** Settings: theme, accent color, glass, contrast and grid size. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = hiltViewModel()
) {
    val c = Brutal.colors
    val bottom = LocalBottomInset.current
    val s by vm.settings.collectAsStateWithLifecycle()

    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp, bottom = bottom + 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BrutalIconButton(onClick = onBack, contentDescription = "Go back", fill = c.surface) {
                BrutalIcon(Glyph.Back)
            }
            BrutalHeading("Settings", style = BrutalType.h1)
        }

        Label("Theme")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeMode.values().forEach { mode ->
                BrutalChip(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = s.themeMode == mode,
                    onClick = { vm.setTheme(mode) }
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        Label("Accent color")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Palette.AccentPresets.forEachIndexed { index, color ->
                val selected = s.accentArgb == color.toArgb()
                BrutalIconButton(
                    onClick = { vm.setAccent(color.toArgb()) },
                    contentDescription = "${ACCENT_NAMES[index]} accent" + if (selected) ", selected" else "",
                    fill = color,
                    size = 52.dp
                ) {
                    if (selected) BrutalIcon(Glyph.Check, tint = Color.Black, size = 26.dp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        DeferredSlider(
            label = "Glass intensity",
            saved = s.glassIntensity,
            range = 0f..1f,
            format = { "${(it * 100).roundToInt()}%" },
            onCommit = { vm.setGlass(it) }
        )

        Label("Photos per row")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (2..6).forEach { n ->
                BrutalChip(text = "$n", selected = s.columns == n, onClick = { vm.setColumns(n) })
            }
        }
        Text(
            "Tip: pinch the photo grid with two fingers to change this.",
            style = BrutalType.caption,
            color = c.textMuted,
            modifier = Modifier.padding(start = 20.dp, top = 6.dp, bottom = 18.dp)
        )

        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text("HIGH CONTRAST", style = BrutalType.label, color = c.text)
                Text(
                    "Solid panels, thicker borders. Easier to read.",
                    style = BrutalType.caption,
                    color = c.textMuted
                )
            }
            BrutalSwitch(checked = s.highContrast, onCheckedChange = vm::setHighContrast)
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text.uppercase(),
        style = BrutalType.label,
        color = Brutal.colors.text,
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
    )
}

/** A slider that moves freely while you drag and saves when you let go. */
@Composable
private fun DeferredSlider(
    label: String,
    saved: Float,
    range: ClosedFloatingPointRange<Float>,
    format: (Float) -> String,
    onCommit: (Float) -> Unit
) {
    val c = Brutal.colors
    var local by remember(saved) { mutableFloatStateOf(saved) }
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 26.dp, bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), style = BrutalType.label, color = c.text, modifier = Modifier.weight(1f))
            Text(format(local), style = BrutalType.label, color = c.textMuted)
        }
        BrutalSlider(
            value = local,
            onValueChange = { local = it },
            valueRange = range,
            onValueChangeFinished = { onCommit(local) }
        )
    }
}
