package com.media.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    audioCount: Int,
    videoCount: Int,
    onClose: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(MediaColors.Ink).statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Space.sm, Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClose) { Icon(Icons.Filled.ArrowBack, "Back", tint = MediaColors.Cream) }
        }

        // Profile block
        Column(Modifier.fillMaxWidth().padding(Space.xl, Space.sm, Space.xl, Space.xl)) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(MediaColors.InkRaised),
                contentAlignment = Alignment.Center
            ) {
                Text("M", style = MaterialTheme.typography.displaySmall, color = MediaColors.Cream)
            }
            Spacer(Modifier.height(Space.md))
            Text("Your library", style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream)
            Text("$audioCount tracks · $videoCount videos",
                style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim)
        }

        SectionLabel("Appearance")
        SettingRow(Icons.Outlined.DarkMode, "Theme", "Dark") {}
        SettingRow(Icons.Outlined.TextFields, "Display", "Editorial") {}

        SectionLabel("Library")
        SettingRow(Icons.Outlined.Storage, "Storage", "$audioCount + $videoCount items") {}
        SettingRow(Icons.Outlined.Refresh, "Rescan device", null) {}

        SectionLabel("About")
        SettingRow(Icons.Outlined.Info, "Version", "1.0") {}
        SettingRow(Icons.Outlined.Shield, "Privacy", null) {}

        Spacer(Modifier.height(40.dp))
        Text("Media", style = MaterialTheme.typography.displaySmall,
            color = MediaColors.CreamFaint,
            modifier = Modifier.padding(Space.xl))
        Text("All your media. One home.",
            style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamFaint,
            modifier = Modifier.padding(start = Space.xl, bottom = 40.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream,
        modifier = Modifier.padding(Space.xl, Space.lg, Space.xl, Space.xs))
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(Space.xl, Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MediaColors.CreamDim, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Space.md))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MediaColors.Cream,
            modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim)
            Spacer(Modifier.width(Space.sm))
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MediaColors.CreamFaint, modifier = Modifier.size(18.dp))
    }
}
