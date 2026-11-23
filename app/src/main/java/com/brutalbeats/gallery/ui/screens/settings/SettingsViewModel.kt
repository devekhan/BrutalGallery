package com.brutalbeats.gallery.ui.screens.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brutalbeats.gallery.data.AppSettings
import com.brutalbeats.gallery.data.SettingsStore
import com.brutalbeats.gallery.data.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(mode: ThemeMode) = write { it[SettingsStore.Keys.THEME] = mode.name }
    fun setHighContrast(on: Boolean) = write { it[SettingsStore.Keys.HIGH_CONTRAST] = on }
    fun setAccent(argb: Int) = write { it[SettingsStore.Keys.ACCENT] = argb }
    fun setGlass(value: Float) = write { it[SettingsStore.Keys.GLASS] = value.coerceIn(0f, 1f) }
    fun setColumns(value: Int) = write { it[SettingsStore.Keys.COLUMNS] = value.coerceIn(2, 6) }

    private fun write(block: (MutablePreferences) -> Unit) {
        viewModelScope.launch { store.edit(block) }
    }
}
