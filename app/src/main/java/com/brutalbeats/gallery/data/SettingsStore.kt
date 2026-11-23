package com.brutalbeats.gallery.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val highContrast: Boolean = false,
    val accentArgb: Int = 0xFFFF4D8D.toInt(),
    val glassIntensity: Float = 0.6f,
    /** Photos per row in the grid (2-6). */
    val columns: Int = 3
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gallery_settings")

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val ACCENT = intPreferencesKey("accent_argb")
        val GLASS = floatPreferencesKey("glass_intensity")
        val COLUMNS = intPreferencesKey("columns")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            AppSettings(
                themeMode = ThemeMode.values().firstOrNull { it.name == p[Keys.THEME] } ?: ThemeMode.SYSTEM,
                highContrast = p[Keys.HIGH_CONTRAST] ?: false,
                accentArgb = p[Keys.ACCENT] ?: 0xFFFF4D8D.toInt(),
                glassIntensity = p[Keys.GLASS] ?: 0.6f,
                columns = (p[Keys.COLUMNS] ?: 3).coerceIn(2, 6)
            )
        }
        .distinctUntilChanged()

    suspend fun edit(block: (MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }
}
