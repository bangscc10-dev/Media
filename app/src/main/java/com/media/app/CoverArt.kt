package com.media.app

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Deterministic warm-muted tint from a title, for the fallback tile.
private fun tintFor(seed: String): Color {
    val palettes = listOf(
        Color(0xFF2D3561), Color(0xFF1F4037), Color(0xFF4A2C3D),
        Color(0xFF243B55), Color(0xFF3D3A24), Color(0xFF3A2A4D),
        Color(0xFF4D2F2A), Color(0xFF2A3D3A)
    )
    val idx = (seed.sumOf { it.code } % palettes.size)
    return palettes[if (idx < 0) idx + palettes.size else idx]
}

@Composable
fun CoverArt(
    item: AppMediaItem,
    modifier: Modifier = Modifier,
    corner: Int = 12
) {
    val context = LocalContext.current
    var art by remember(item.uri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(item.uri) {
        art = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, item.uri)
                val bytes = retriever.embeddedPicture
                retriever.release()
                bytes?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(corner.dp)),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = art
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                Modifier.fillMaxSize().background(tintFor(item.title)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.firstOrNull()?.uppercase() ?: "M",
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontSize = 34.sp,
                    color = MediaColors.Cream.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
