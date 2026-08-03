package com.media.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF7C5CFF)

private val DarkColors = darkColorScheme(
    primary = Accent,
    background = Color(0xFF0B0B0F),
    surface = Color(0xFF16161C),
)

private val LightColors = lightColorScheme(
    primary = Accent,
)

@Composable
fun MediaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
