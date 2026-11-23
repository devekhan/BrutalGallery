package com.brutalbeats.gallery.data

/** One photo or video from the phone's media library. */
data class GalleryItem(
    val id: Long,
    /** content:// address of the file. */
    val uri: String,
    val isVideo: Boolean,
    val displayName: String,
    val mimeType: String,
    /** When it was taken (falls back to when it was added). */
    val takenMs: Long,
    val dateAddedSec: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val bucketId: Long,
    val bucketName: String,
    val relativePath: String
)

/** A folder on the phone (Camera, Screenshots, WhatsApp Images...). */
data class AlbumInfo(
    val bucketId: Long,
    val name: String,
    val path: String,
    val count: Int,
    val videoCount: Int,
    val cover: GalleryItem
)
