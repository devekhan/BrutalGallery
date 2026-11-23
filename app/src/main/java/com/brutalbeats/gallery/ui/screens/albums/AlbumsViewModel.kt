package com.brutalbeats.gallery.ui.screens.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brutalbeats.gallery.data.AlbumInfo
import com.brutalbeats.gallery.data.MediaFilter
import com.brutalbeats.gallery.data.MediaRepository
import com.brutalbeats.gallery.data.SettingsStore
import com.brutalbeats.gallery.data.TimelineEntry
import com.brutalbeats.gallery.data.buildAlbums
import com.brutalbeats.gallery.data.buildTimeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repo: MediaRepository,
    private val store: SettingsStore
) : ViewModel() {

    /** null = still loading. */
    val albums: StateFlow<List<AlbumInfo>?> = repo.items
        .map { items -> items?.let { buildAlbums(it) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val columns: StateFlow<Int> = store.settings.map { it.columns }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 3)

    /** The date-grouped photos of one folder. */
    fun albumEntries(bucketId: Long): Flow<List<TimelineEntry>?> = repo.items
        .map { items ->
            items?.let { buildTimeline(it.filter { item -> item.bucketId == bucketId }, MediaFilter.ALL) }
        }
        .flowOn(Dispatchers.Default)

    fun setColumns(value: Int) {
        viewModelScope.launch { store.edit { it[SettingsStore.Keys.COLUMNS] = value.coerceIn(2, 6) } }
    }
}
