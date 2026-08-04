package com.media.app
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun InfoScaffold(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(MediaColors.Ink).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(Space.sm, Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MediaColors.Cream) }
            Text(title, style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(Space.xl, Space.sm, Space.xl, 40.dp),
            content = content
        )
    }
}

@Composable
private fun Para(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = MediaColors.CreamDim,
        modifier = Modifier.padding(bottom = Space.md))
}

@Composable
private fun Heading(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream,
        modifier = Modifier.padding(top = Space.md, bottom = Space.sm))
}

@Composable
fun AboutScreen(version: String, onClose: () -> Unit) {
    InfoScaffold("About", onClose) {
        Text("Media", style = MaterialTheme.typography.displaySmall, color = MediaColors.Cream,
            modifier = Modifier.padding(bottom = Space.xs))
        Text("All your media. One home.", style = MaterialTheme.typography.bodyLarge,
            color = MediaColors.CreamDim, modifier = Modifier.padding(bottom = Space.xl))

        Para("Media brings your music, podcasts, video, and audiobooks together into one calm, considered home — so your library stops feeling scattered across a dozen apps.")
        Para("Everything plays locally from your device. No accounts, no tracking, no clutter.")

        Heading("Version")
        Para(version)

        Heading("Built with")
        Para("Kotlin, Jetpack Compose, and Media3. Type set in Fraunces and Inter.")

        Spacer(Modifier.height(Space.xl))
        Text("All your media. One home.", style = MaterialTheme.typography.bodyMedium,
            color = MediaColors.CreamFaint)
    }
}

@Composable
fun TermsScreen(onClose: () -> Unit) {
    InfoScaffold("Terms of Use", onClose) {
        Para("By using Media, you agree to these terms. They are intentionally simple.")

        Heading("The app")
        Para("Media is a free media player that organizes and plays media files already on your device. It is provided as-is, without warranty of any kind.")

        Heading("Your content")
        Para("Media does not upload, share, or take ownership of your files. Your media and your edits stay on your device and belong to you.")

        Heading("Acceptable use")
        Para("Use Media only with content you have the right to play. You are responsible for the media you add to your device.")

        Heading("Liability")
        Para("The developer is not liable for any loss or damage arising from use of the app, to the extent permitted by law.")

        Heading("Changes")
        Para("These terms may be updated over time. Continued use of the app means you accept the current terms.")
    }
}
