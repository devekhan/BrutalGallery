package com.brutalbeats.gallery.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.brutalbeats.gallery.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for the phone's photos and videos.
 * Reads Android's media library and watches it, so new photos appear on their own.
 */
@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val _items = MutableStateFlow<List<GalleryItem>?>(null)

    /** Newest first. null = still loading. */
    val items: StateFlow<List<GalleryItem>?> = _items.asStateFlow()

    private val loadLock = Mutex()
    private var started = false
    private var pending: Job? = null

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            scheduleRefresh()
        }
    }

    /** Safe to call many times. Call once photo access has been granted. */
    fun start() {
        if (started) {
            refresh()
            return
        }
        started = true
        val resolver: ContentResolver = context.contentResolver
        resolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer)
        resolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer)
        refresh()
    }

    fun refresh() {
        scope.launch(Dispatchers.IO) { load() }
    }

    private fun scheduleRefresh() {
        pending?.cancel()
        pending = scope.launch {
            delay(600)
            withKeepAlive { load() }
        }
    }

    private suspend fun withKeepAlive(block: suspend () -> Unit) = block()

    private suspend fun load() {
        loadLock.withLock {
            _items.value = try {
                query()
            } catch (e: SecurityException) {
                emptyList()
            } catch (e: Exception) {
                _items.value ?: emptyList()
            }
        }
    }

    private fun query(): List<GalleryItem> {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        val out = ArrayList<GalleryItem>()
        val cursor = context.contentResolver.query(collection, projection, selection, args, null)
            ?: throw IllegalStateException("Could not read the media library.")
        cursor.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val iType = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val iName = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val iMime = c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val iTaken = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            val iAdded = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val iSize = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val iWidth = c.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val iHeight = c.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val iDuration = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val iBucketId = c.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
            val iBucketName = c.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val iPath = c.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)

            while (c.moveToNext()) {
                val size = c.getLong(iSize)
                if (size <= 0L) continue
                val path = c.getString(iPath).orEmpty()
                // Skip hidden dot-folders such as ".thumbnails" or WhatsApp ".Statuses".
                if (path.split('/').any { it.startsWith(".") && it.length > 1 }) continue

                val id = c.getLong(iId)
                val isVideo = c.getInt(iType) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val base = if (isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val added = c.getLong(iAdded)
                val taken = c.getLong(iTaken).takeIf { it > 0L } ?: (added * 1000L)
                val bucketName = c.getString(iBucketName).orEmpty().ifBlank { "Other" }

                out.add(
                    GalleryItem(
                        id = id,
                        uri = ContentUris.withAppendedId(base, id).toString(),
                        isVideo = isVideo,
                        displayName = c.getString(iName).orEmpty(),
                        mimeType = c.getString(iMime) ?: if (isVideo) "video/*" else "image/*",
                        takenMs = taken,
                        dateAddedSec = added,
                        sizeBytes = size,
                        width = c.getInt(iWidth),
                        height = c.getInt(iHeight),
                        durationMs = if (isVideo) c.getLong(iDuration) else 0L,
                        bucketId = c.getLong(iBucketId),
                        bucketName = bucketName,
                        relativePath = path
                    )
                )
            }
        }
        out.sortWith(compareByDescending<GalleryItem> { it.takenMs }.thenByDescending { it.id })
        return out
    }
}

/** Groups items into folders. Camera and Screenshots first, then the most recently used. */
fun buildAlbums(items: List<GalleryItem>): List<AlbumInfo> =
    items.groupBy { it.bucketId }.map { (bucketId, group) ->
        val newest = group.first() // items are already newest-first
        AlbumInfo(
            bucketId = bucketId,
            name = newest.bucketName,
            path = newest.relativePath,
            count = group.size,
            videoCount = group.count { it.isVideo },
            cover = newest
        )
    }.sortedWith(
        compareBy<AlbumInfo> {
            when (it.name.lowercase()) {
                "camera" -> 0
                "screenshots" -> 1
                else -> 2
            }
        }.thenByDescending { it.cover.takenMs }
    )
