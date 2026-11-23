package com.brutalbeats.gallery.ui.nav

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Extra bottom space screens leave for the bottom bar. Provided by the root layout. */
val LocalBottomInset = compositionLocalOf<Dp> { 0.dp }

enum class TopTab(val label: String) {
    PHOTOS("Photos"),
    ALBUMS("Albums"),
    LIBRARY("Library")
}

/** Pages opened on top of a tab. */
sealed interface Page {
    data class Album(val bucketId: Long, val name: String) : Page
    data object Settings : Page
}

/** Tiny navigation state: the selected tab plus a back stack of pages on top of it. */
@Stable
class Navigator {
    var tab by mutableStateOf(TopTab.PHOTOS)
        private set
    val stack = mutableStateListOf<Page>()

    fun select(target: TopTab) {
        stack.clear()
        tab = target
    }

    fun push(page: Page) {
        stack.add(page)
    }

    /** Returns true if a page was closed. */
    fun pop(): Boolean {
        if (stack.isEmpty()) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
}
