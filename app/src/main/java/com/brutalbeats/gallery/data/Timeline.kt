package com.brutalbeats.gallery.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MediaFilter(val label: String) {
    ALL("All"),
    PHOTOS("Photos"),
    VIDEOS("Videos")
}

/** A row of the photo grid: either a date heading or one photo/video. */
sealed interface TimelineEntry {
    val key: String

    data class Header(val label: String, val count: Int, override val key: String) : TimelineEntry

    data class Media(val item: GalleryItem) : TimelineEntry {
        override val key: String get() = "m${item.id}"
    }
}

/** Newest-first list grouped under "Today", "Yesterday", then dates. */
fun buildTimeline(items: List<GalleryItem>, filter: MediaFilter): List<TimelineEntry> {
    val filtered = when (filter) {
        MediaFilter.ALL -> items
        MediaFilter.PHOTOS -> items.filter { !it.isVideo }
        MediaFilter.VIDEOS -> items.filter { it.isVideo }
    }
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val sameYear = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
    val otherYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    fun label(day: LocalDate): String = when {
        day == today -> "Today"
        day == today.minusDays(1) -> "Yesterday"
        day.year == today.year -> day.format(sameYear)
        else -> day.format(otherYear)
    }

    val grouped = filtered.groupBy {
        Instant.ofEpochMilli(it.takenMs).atZone(zone).toLocalDate()
    }
    val out = ArrayList<TimelineEntry>(filtered.size + grouped.size)
    for ((day, list) in grouped) {
        out.add(TimelineEntry.Header(label(day), list.size, "h${day.toEpochDay()}"))
        list.forEach { out.add(TimelineEntry.Media(it)) }
    }
    return out
}
