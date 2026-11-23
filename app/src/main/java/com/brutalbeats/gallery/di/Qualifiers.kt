package com.brutalbeats.gallery.di

import javax.inject.Qualifier

/** Marks the app-wide coroutine scope (lives as long as the process). */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
