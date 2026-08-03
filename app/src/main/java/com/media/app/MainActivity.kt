package com.media.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.MoreExecutors
import android.content.ComponentName
import androidx.media3.session.SessionToken

class MainActivity : ComponentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MediaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

private fun requiredPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

@UnstableApi
@Composable
fun AppRoot(vm: PlayerViewModel = viewModel()) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            requiredPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> granted = result.values.all { it } }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(requiredPermissions())
    }

    if (!granted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Media needs permission to read your files", color = Color.White)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { launcher.launch(requiredPermissions()) }) {
                    Text("Grant access")
                }
            }
        }
        return
    }

    MainScaffold(vm)
}

private enum class Tab(val label: String) { AUDIO("Audio"), VIDEO("Video") }

@UnstableApi
@Composable
fun MainScaffold(vm: PlayerViewModel) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    var tab by remember { mutableStateOf(Tab.AUDIO) }
    var showPlayer by remember { mutableStateOf(false) }

    val audio by remember { mutableStateOf(MediaRepository.loadAudio(context)) }
    val video by remember { mutableStateOf(MediaRepository.loadVideo(context)) }
    val list = if (tab == Tab.AUDIO) audio else video

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = {
                            Icon(
                                if (t == Tab.AUDIO) Icons.Filled.MusicNote else Icons.Filled.Movie,
                                contentDescription = t.label
                            )
                        },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Media",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(Modifier.weight(1f)) {
                items(list) { item ->
                    MediaRow(item) {
                        val idx = list.indexOf(item)
                        vm.play(list, idx)
                        if (tab == Tab.VIDEO) showPlayer = true
                    }
                }
            }
            if (state.hasItem) {
                NowPlayingBar(state, vm) { showPlayer = true }
            }
        }
    }

    if (showPlayer) {
        PlayerScreen(state, vm) { showPlayer = false }
    }
}

@Composable
fun MediaRow(item: AppMediaItem, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (item.type == MediaType.AUDIO) Icons.Filled.MusicNote else Icons.Filled.Movie,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                item.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NowPlayingBar(state: PlayerState, vm: PlayerViewModel, onExpand: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onExpand)
            .padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(state.currentTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                state.currentArtist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )
        }
        IconButton(onClick = { vm.togglePlayPause() }) {
            Icon(
                if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause"
            )
        }
    }
}

@UnstableApi
@Composable
fun PlayerScreen(state: PlayerState, vm: PlayerViewModel, onClose: () -> Unit) {
    val context = LocalContext.current

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    val token = SessionToken(ctx, ComponentName(ctx, PlaybackService::class.java))
                    val future = MediaController.Builder(ctx, token).buildAsync()
                    future.addListener({
                        player = future.get()
                    }, MoreExecutors.directExecutor())
                }
            },
            modifier = Modifier.fillMaxWidth().align(Alignment.Center)
        )

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp)
        ) {
            Text(state.currentTitle, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(state.currentArtist, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            if (state.durationMs > 0) {
                Slider(
                    value = state.positionMs.toFloat().coerceIn(0f, state.durationMs.toFloat()),
                    onValueChange = { vm.seekTo(it.toLong()) },
                    valueRange = 0f..state.durationMs.toFloat()
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.previous() }) {
                    Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White)
                }
                IconButton(onClick = { vm.togglePlayPause() }) {
                    Icon(
                        if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { vm.next() }) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = Color.White)
                }
            }
        }

        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
            Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = Color.White)
        }
    }
}
