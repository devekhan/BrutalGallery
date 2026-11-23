package com.brutalbeats.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brutalbeats.gallery.data.AppSettings
import com.brutalbeats.gallery.data.MediaRepository
import com.brutalbeats.gallery.data.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repo: MediaRepository,
    store: SettingsStore
) : ViewModel() {

    /** null until the saved settings are read (a split second at launch). */
    val settings: StateFlow<AppSettings?> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Starts reading the library. Call once photo access is granted. */
    fun startLibrary() = repo.start()
}
