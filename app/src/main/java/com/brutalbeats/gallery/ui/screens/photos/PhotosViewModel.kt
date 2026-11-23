package com.brutalbeats.gallery.ui.screens.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brutalbeats.gallery.data.MediaFilter
import com.brutalbeats.gallery.data.MediaRepository
import com.brutalbeats.gallery.data.SettingsStore
import com.brutalbeats.gallery.data.TimelineEntry
import com.brutalbeats.gallery.data.buildTimeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val repo: MediaRepository,
    private val store: SettingsStore
) : ViewModel() {

    val filter = MutableStateFlow(MediaFilter.ALL)

    /** null = still loading. */
    val entries: StateFlow<List<TimelineEntry>?> = combine(repo.items, filter) { items, f ->
        items?.let { buildTimeline(it, f) }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val total: StateFlow<Int> = repo.items.map { it?.size ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val columns: StateFlow<Int> = store.settings.map { it.columns }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 3)

    fun setFilter(value: MediaFilter) {
        filter.value = value
    }

    fun setColumns(value: Int) {
        viewModelScope.launch { store.edit { it[SettingsStore.Keys.COLUMNS] = value.coerceIn(2, 6) } }
    }

    fun refresh() = repo.refresh()
}
