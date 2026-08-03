package com.media.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PodcastsScreen(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MediaColors.Ink).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(Space.sm, Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClose) { Icon(Icons.Filled.ArrowBack, "Back", tint = MediaColors.Cream) }
            Text("Podcasts", style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Space.xl)
            ) {
                Icon(Icons.Outlined.Podcasts, null, tint = MediaColors.CreamFaint,
                    modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(Space.lg))
                Text("No podcasts yet", style = MaterialTheme.typography.titleLarge,
                    color = MediaColors.Cream)
                Spacer(Modifier.height(Space.sm))
                Text("Subscribe to shows to see them here.",
                    style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim)
            }
        }
    }
}
