package com.media.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "media_settings")

enum class ThemeMode { DARK, LIGHT, SYSTEM }

data class MediaSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val fontScale: Float = 1.0f   // 0.9 = compact, 1.0 = default, 1.15 = large
)

object SettingsStore {
    private val THEME = stringPreferencesKey("theme_mode")
    private val FONT = floatPreferencesKey("font_scale")

    fun flow(context: Context): Flow<MediaSettings> =
        context.dataStore.data.map { p ->
            MediaSettings(
                themeMode = when (p[THEME]) {
                    "LIGHT" -> ThemeMode.LIGHT
                    "SYSTEM" -> ThemeMode.SYSTEM
                    "DARK" -> ThemeMode.DARK
                    else -> ThemeMode.LIGHT
                },
                fontScale = p[FONT] ?: 1.0f
            )
        }

    suspend fun setTheme(context: Context, mode: ThemeMode) {
        context.dataStore.edit { it[THEME] = mode.name }
    }

    suspend fun setFontScale(context: Context, scale: Float) {
        context.dataStore.edit { it[FONT] = scale }
    }
}
