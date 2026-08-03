package com.media.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Semantic color tokens (resolved per theme) ----
class Palette(
    val bg: Color,          // background
    val raised: Color,      // cards, sheets
    val hairline: Color,    // dividers, borders
    val text: Color,        // primary text
    val textDim: Color,     // secondary
    val textFaint: Color,   // tertiary / labels
    val accent: Color,      // active state, progress
    val onAccent: Color,    // text/icon on accent
    val onInverse: Color    // e.g. icon on the cream play button
)

// Dark — the original editorial ink/cream
val DarkPalette = Palette(
    bg = Color(0xFF0B0B0F),
    raised = Color(0xFF16161C),
    hairline = Color(0xFF1C1C22),
    text = Color(0xFFF4EFE6),
    textDim = Color(0xFF9B968C),
    textFaint = Color(0xFF6F6B63),
    accent = Color(0xFF7C5CFF),
    onAccent = Color(0xFFF4EFE6),
    onInverse = Color(0xFF0B0B0F)
)

// Light — warm editorial paper, ink text. Restrained, not stark white.
val LightPalette = Palette(
    bg = Color(0xFFF7F3EC),        // warm paper
    raised = Color(0xFFFFFFFF),    // clean card
    hairline = Color(0xFFE4DED3),  // soft rule
    text = Color(0xFF1A1814),      // near-black ink
    textDim = Color(0xFF6B655B),   // warm grey
    textFaint = Color(0xFF9C958A),
    accent = Color(0xFF6A48E0),    // slightly deeper purple for contrast on light
    onAccent = Color(0xFFFFFFFF),
    onInverse = Color(0xFF1A1814)
)

// Current palette, provided via CompositionLocal so any composable can read it.
val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { DarkPalette }

// Back-compat shim: existing code references MediaColors.X — map to current palette.
object MediaColors {
    val Ink @Composable get() = LocalPalette.current.bg
    val InkRaised @Composable get() = LocalPalette.current.raised
    val InkHairline @Composable get() = LocalPalette.current.hairline
    val Cream @Composable get() = LocalPalette.current.text
    val CreamDim @Composable get() = LocalPalette.current.textDim
    val CreamFaint @Composable get() = LocalPalette.current.textFaint
    val Accent @Composable get() = LocalPalette.current.accent
    val OnInverse @Composable get() = LocalPalette.current.onInverse
}

val Fraunces = FontFamily(Font(R.font.fraunces_variable))
val Inter = FontFamily(Font(R.font.inter_variable))

private fun typography(scale: Float) = Typography(
    displaySmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = (26 * scale).sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = (18 * scale).sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = (13 * scale).sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = (15 * scale).sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = (11.5f * scale).sp),
    labelSmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = (9.5f * scale).sp, letterSpacing = 0.3.sp),
)

object Space {
    val xs = 4.dp; val sm = 8.dp; val md = 12.dp
    val lg = 16.dp; val xl = 22.dp; val xxl = 32.dp
}

@Composable
fun MediaTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(primary = palette.accent, background = palette.bg,
            surface = palette.raised, onBackground = palette.text,
            onSurface = palette.text, onPrimary = palette.onAccent)
    } else {
        lightColorScheme(primary = palette.accent, background = palette.bg,
            surface = palette.raised, onBackground = palette.text,
            onSurface = palette.text, onPrimary = palette.onAccent)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = scheme, typography = typography(fontScale), content = content)
    }
}
