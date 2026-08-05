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

        Para("Your library shouldn\'t be scattered across a dozen apps. Media gathers everything that plays on your phone into one calm, editorial home — and gets out of your way so you can just listen and watch.")

        Heading("Four kinds of media, one place")
        Para("Music for your songs. Podcasts for long-form talk. Audiobooks for the books you listen to. And video, in any format your phone understands. Media sorts them automatically, and you can always reorganize anything by hand.")

        Heading("Yours, and only yours")
        Para("Everything plays locally, straight from your device. There are no accounts to make, nothing to sign in to, and no ads. Media collects no data and tracks nothing — your library, your history, and your edits never leave your phone.")

        Heading("Made to feel calm")
        Para("Ink and warm cream. A serif that reads like a magazine. Color comes from your album art, not from the app shouting for attention. Every screen is built to be quiet, considered, and pleasant to live in day after day.")

        Heading("Little things that matter")
        Para("A sleep timer that fades out gently. Audiobooks and podcasts that remember exactly where you stopped, and the speed you like them at. A now-playing screen you can pull down to dismiss. The details you\'d expect from a player that respects your time.")

        Spacer(Modifier.height(Space.lg))
        Text("Version $version", style = MaterialTheme.typography.bodyMedium,
            color = MediaColors.CreamFaint)
        Spacer(Modifier.height(Space.sm))
        Text("Made with care for people who love their media.",
            style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamFaint)
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
