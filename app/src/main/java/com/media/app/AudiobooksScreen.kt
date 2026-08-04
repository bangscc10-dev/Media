package com.media.app
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AudiobooksScreen(
    audiobooks: List<AppMediaItem>,
    state: PlayerState,
    onPlay: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MediaColors.Ink).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(Space.sm, Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MediaColors.Cream) }
            Text("Audiobooks", style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream)
        }

        if (audiobooks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Space.xl)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.MenuBook, null, tint = MediaColors.CreamFaint,
                        modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(Space.lg))
                    Text("No audiobooks yet", style = MaterialTheme.typography.titleLarge,
                        color = MediaColors.Cream)
                    Spacer(Modifier.height(Space.sm))
                    Text("Files in an Audiobooks folder, or anything you mark as an audiobook, show up here.",
                        style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(Space.xl, Space.md, Space.xl, bottomSafePadding()),
                horizontalArrangement = Arrangement.spacedBy(Space.md),
                verticalArrangement = Arrangement.spacedBy(Space.lg)
            ) {
                items(audiobooks.size) { idx ->
                    val item = audiobooks[idx]
                    val isActive = state.currentUri == item.uri.toString() && state.isPlaying
                    Column(Modifier.clickable { onPlay(idx) }) {
                        Box {
                            CoverArt(item, Modifier.fillMaxWidth().aspectRatio(1f), corner = 12)
                            Box(
                                Modifier.align(Alignment.BottomEnd).padding(Space.sm)
                                    .size(34.dp).clip(CircleShape).background(MediaColors.Cream),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    if (isActive) "Pause" else "Play",
                                    tint = MediaColors.OnInverse, modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(Space.sm))
                        Text(item.title, style = MaterialTheme.typography.titleMedium,
                            color = MediaColors.Cream, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.artist, style = MaterialTheme.typography.bodyMedium,
                            color = MediaColors.CreamDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
