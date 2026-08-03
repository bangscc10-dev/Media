package com.media.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Color tokens ----
object MediaColors {
    val Ink = Color(0xFF0B0B0F)          // background
    val InkRaised = Color(0xFF16161C)     // cards, sheets
    val InkHairline = Color(0xFF1C1C22)   // dividers, borders
    val Cream = Color(0xFFF4EFE6)         // primary text
    val CreamDim = Color(0xFF9B968C)      // secondary text
    val CreamFaint = Color(0xFF6F6B63)    // tertiary / labels
    val Accent = Color(0xFF7C5CFF)        // active state, progress ONLY
}

// ---- Typefaces ----
val Fraunces = FontFamily(Font(R.font.fraunces_variable))
val Inter = FontFamily(Font(R.font.inter_variable))

// ---- Type scale ----
val MediaTypography = Typography(
    // Serif display — wordmark, big headers
    displaySmall = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 26.sp, letterSpacing = (-0.5).sp
    ),
    // Serif — section / pillar headers
    titleLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, letterSpacing = (-0.2).sp
    ),
    // Sans — card titles
    titleMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    // Sans — body
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    // Sans — metadata, secondary
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp
    ),
    // Sans — tiny labels
    labelSmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 9.5.sp, letterSpacing = 0.3.sp
    ),
)

// ---- Spacing ----
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 22.dp
    val xxl = 32.dp
}

private val MediaColorScheme = darkColorScheme(
    primary = MediaColors.Accent,
    background = MediaColors.Ink,
    surface = MediaColors.InkRaised,
    onBackground = MediaColors.Cream,
    onSurface = MediaColors.Cream,
    onPrimary = MediaColors.Ink,
)

@Composable
fun MediaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MediaColorScheme,
        typography = MediaTypography,
        content = content
    )
}
