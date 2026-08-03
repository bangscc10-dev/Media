package com.media.app

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        var keep = true
        splash.setKeepOnScreenCondition { keep }
        window.decorView.postDelayed({ keep = false }, 850)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settings by SettingsStore.flow(this).collectAsState(initial = MediaSettings())
            val dark = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                else -> true
            }
            SetStatusBarIcons(dark)
            MediaTheme(themeMode = settings.themeMode, fontScale = settings.fontScale) {
                Surface(Modifier.fillMaxSize(), color = MediaColors.Ink) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun SetStatusBarIcons(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Dark theme -> light icons; Light theme -> dark icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
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
        mutableStateOf(requiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> granted = result.values.all { it } }

    LaunchedEffect(Unit) { if (!granted) launcher.launch(requiredPermissions()) }

    if (!granted) {
        PermissionGate { launcher.launch(requiredPermissions()) }
        return
    }
    HomeScaffold(vm)
}

@Composable
private fun PermissionGate(onGrant: () -> Unit) {
    Box(Modifier.fillMaxSize().background(MediaColors.Ink), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(Space.xl)) {
            Text("Media", style = MaterialTheme.typography.displaySmall, color = MediaColors.Cream)
            Spacer(Modifier.height(Space.md))
            Text(
                "All your music, podcasts, video, and audiobooks in one home.",
                style = MaterialTheme.typography.bodyLarge,
                color = MediaColors.CreamDim
            )
            Spacer(Modifier.height(Space.xl))
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediaColors.Cream, contentColor = MediaColors.Ink
                )
            ) { Text("Grant access") }
        }
    }
}

@UnstableApi
@Composable
fun HomeScaffold(vm: PlayerViewModel) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    var showPlayer by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPodcasts by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }
    var libraryPillar by remember { mutableStateOf<Pillar?>(null) }
    var showAudiobooks by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    val db = remember { OverrideDatabase.get(context) }
    val scope = rememberCoroutineScope()
    val settings by SettingsStore.flow(context).collectAsState(initial = MediaSettings())

    val recentHistory by remember {
        db.historyDao().observeRecent(10)
    }.collectAsState(initial = emptyList())

    // Record a play (after 5s) into history; upsert = auto-dedup + move to front by timestamp.
    LaunchedEffect(Unit) {
        vm.onQualifyingPlay = { mediaId ->
            scope.launch {
                db.historyDao().record(PlayHistory(mediaId, System.currentTimeMillis()))
            }
        }
    }
    val overrides by remember {
        db.dao().observeAll().map { list -> list.associateBy { it.mediaId } }
    }.collectAsState(initial = emptyMap())

    val allAudio = remember(overrides, reloadKey) { MediaRepository.audioWithOverrides(context, overrides) }
    val music = remember(allAudio) { allAudio.filter { it.pillar == Pillar.MUSIC } }
    val podcasts = remember(allAudio) { allAudio.filter { it.pillar == Pillar.PODCAST } }
    val audiobooks = remember(allAudio) { allAudio.filter { it.pillar == Pillar.AUDIOBOOK } }
    val video = remember(reloadKey) { MediaRepository.loadVideo(context) }

    // Edit sheet state
    var editItem by remember { mutableStateOf<AppMediaItem?>(null) }

    val continueItems = remember(recentHistory, music, podcasts, audiobooks, video) {
        val byId = (music + podcasts + audiobooks + video).associateBy { it.id }
        recentHistory.mapNotNull { byId[it.mediaId] }
    }

    Box(Modifier.fillMaxSize().background(MediaColors.Ink)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 170.dp),
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            item { HomeHeader(onSearch = { showSearch = true }, onAccount = { showSettings = true }) }

            if (continueItems.isNotEmpty()) {
                item {
                    ShelfHeader("Continue")
                    MediaShelf(continueItems, state, large = true, onEdit = { editItem = it }) { idx ->
                        vm.playOrToggle(continueItems, idx)
                        if (continueItems[idx].type == MediaType.VIDEO) showPlayer = true
                    }
                }
            }

            item { Divider(color = MediaColors.InkHairline, modifier = Modifier.padding(horizontal = Space.xl)) }

            if (music.isNotEmpty()) {
                item {
                    ShelfHeader("Music", onSeeAll = { libraryPillar = Pillar.MUSIC; showLibrary = true })
                    MediaShelf(music, state, onEdit = { editItem = it }) { idx -> vm.playOrToggle(music, idx) }
                }
            }
            if (podcasts.isNotEmpty()) {
                item {
                    ShelfHeader("Podcasts", onSeeAll = { libraryPillar = Pillar.PODCAST; showLibrary = true })
                    MediaShelf(podcasts, state, onEdit = { editItem = it }) { idx -> vm.playOrToggle(podcasts, idx) }
                }
            }
            if (audiobooks.isNotEmpty()) {
                item {
                    ShelfHeader("Audiobooks", onSeeAll = { libraryPillar = Pillar.AUDIOBOOK; showLibrary = true })
                    MediaShelf(audiobooks, state, onEdit = { editItem = it }) { idx -> vm.playOrToggle(audiobooks, idx) }
                }
            }
            if (video.isNotEmpty()) {
                item {
                    ShelfHeader("Video", onSeeAll = { libraryPillar = Pillar.VIDEO; showLibrary = true })
                    MediaShelf(video, state, wide = true, onEdit = { editItem = it }) { idx ->
                        vm.playOrToggle(video, idx); showPlayer = true
                    }
                }
            }

            if (music.isEmpty() && podcasts.isEmpty() && audiobooks.isEmpty() && video.isEmpty()) {
                item { EmptyState() }
            }
        }

        if (state.hasItem) {
            NowPlayingBar(
                state, vm,
                onExpand = { showPlayer = true },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 66.dp, start = Space.md, end = Space.md)
            )
        }
        BottomBar(Modifier.align(Alignment.BottomCenter), onLibraryTab = { showLibrary = true }, onPodcastsTab = { showPodcasts = true }) { showAudiobooks = true }
    }

    if (showPlayer) {
        FullPlayer(state, vm) { showPlayer = false }
    }
    if (showSearch) {
        SearchScreen(
            all = allAudio + video,
            onPlay = { list, idx ->
                vm.play(list, idx)
                showSearch = false
                if (list[idx].type == MediaType.VIDEO) showPlayer = true
            },
            onClose = { showSearch = false }
        )
    }
    if (showSettings) {
        SettingsScreen(
            audioCount = allAudio.size,
            videoCount = video.size,
            settings = settings,
            onThemeChange = { mode -> scope.launch { SettingsStore.setTheme(context, mode) } },
            onFontScaleChange = { scale -> scope.launch { SettingsStore.setFontScale(context, scale) } },
            onRescan = {
                MediaRepository.refresh()
                reloadKey++
            },
            onOpenTerms = { showTerms = true },
            onOpenAbout = { showAbout = true },
            onClose = { showSettings = false }
        )
    }
    if (showPodcasts) {
        PodcastsScreen(
            podcasts = podcasts,
            state = state,
            onPlay = { idx -> vm.playOrToggle(podcasts, idx) },
            onClose = { showPodcasts = false }
        )
    }
    if (showTerms) {
        TermsScreen(onClose = { showTerms = false })
    }
    if (showAbout) {
        AboutScreen(version = "1.0", onClose = { showAbout = false })
    }
    if (showAudiobooks) {
        AudiobooksScreen(
            audiobooks = audiobooks,
            state = state,
            onPlay = { idx -> vm.playOrToggle(audiobooks, idx) },
            onClose = { showAudiobooks = false }
        )
    }
    if (showLibrary) {
        LibraryScreen(
            all = allAudio + video,
            state = state,
            initialPillar = libraryPillar,
            onPlay = { list, idx ->
                vm.playOrToggle(list, idx)
                if (list[idx].type == MediaType.VIDEO) showPlayer = true
            },
            onEdit = { editItem = it },
            onClose = { showLibrary = false; libraryPillar = null }
        )
    }
    editItem?.let { item ->
        EditSheet(
            item = item,
            hasOverride = overrides.containsKey(item.id),
            onSave = { title, artist, details, pillar ->
                scope.launch {
                    db.dao().upsert(
                        MediaOverride(
                            mediaId = item.id,
                            customTitle = title,
                            customArtist = artist,
                            details = details,
                            pillar = pillar
                        )
                    )
                }
                // Push edit into the live playback session if this item is playing now
                vm.updateCurrentMetadata(item.id, title, artist)
                editItem = null
            },
            onReset = {
                scope.launch { db.dao().delete(item.id) }
                editItem = null
            },
            onDismiss = { editItem = null }
        )
    }
}

@Composable
private fun HomeHeader(onSearch: () -> Unit, onAccount: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(Space.xl, Space.xl, Space.xl, Space.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Media", style = MaterialTheme.typography.displaySmall, color = MediaColors.Cream)
        Row(horizontalArrangement = Arrangement.spacedBy(Space.lg)) {
            Icon(Icons.Outlined.Search, "Search", tint = MediaColors.CreamDim,
                modifier = Modifier.clickable(onClick = onSearch))
            Icon(Icons.Outlined.AccountCircle, "You", tint = MediaColors.CreamDim,
                modifier = Modifier.clickable(onClick = onAccount))
        }
    }
}

@Composable
private fun ShelfHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(Space.xl, Space.lg, Space.xl, Space.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream)
        if (onSeeAll != null) {
            Text("See all", style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim,
                modifier = Modifier.clickable(onClick = onSeeAll))
        }
    }
}

@Composable
private fun MediaShelf(
    items: List<AppMediaItem>,
    state: PlayerState,
    large: Boolean = false,
    wide: Boolean = false,
    onEdit: (AppMediaItem) -> Unit,
    onPlay: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Space.xl, vertical = Space.md),
        horizontalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        items(items.size) { idx ->
            val item = items[idx]
            val isActive = state.currentUri == item.uri.toString()
            MediaCard(item, large = large, wide = wide,
                isPlaying = isActive && state.isPlaying,
                onLongPress = { onEdit(item) }) { onPlay(idx) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaCard(
    item: AppMediaItem,
    large: Boolean,
    wide: Boolean,
    isPlaying: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "press")

    val artW = if (wide) 220.dp else if (large) 150.dp else 118.dp
    val artH = if (wide) 124.dp else artW

    Column(
        Modifier
            .width(artW)
            .scale(scale)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Box {
            CoverArt(item, Modifier.width(artW).height(artH), corner = if (large || wide) 14 else 12)
            Box(
                Modifier.align(Alignment.BottomEnd).padding(Space.sm)
                    .size(34.dp).clip(CircleShape).background(MediaColors.Cream),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (isPlaying) "Pause" else "Play",
                    tint = MediaColors.Ink, modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(Space.sm))
        Text(item.title, style = MaterialTheme.typography.titleMedium, color = MediaColors.Cream,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.artist, style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!item.details.isNullOrBlank()) {
            Text(item.details, style = MaterialTheme.typography.bodyMedium,
                color = MediaColors.CreamFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(Space.xl, 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nothing here yet", style = MaterialTheme.typography.titleLarge, color = MediaColors.Cream)
        Spacer(Modifier.height(Space.sm))
        Text("Add music or video to your device to see it here.",
            style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamDim)
    }
}

@Composable
private fun NowPlayingBar(
    state: PlayerState, vm: PlayerViewModel, onExpand: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(MediaColors.InkRaised).clickable(onClick = onExpand)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Space.md, Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(state.currentTitle, style = MaterialTheme.typography.titleMedium,
                    color = MediaColors.Cream, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.currentArtist, style = MaterialTheme.typography.bodyMedium,
                    color = MediaColors.CreamDim, maxLines = 1)
            }
            IconButton(onClick = { vm.togglePlayPause() }) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "Play/Pause", tint = MediaColors.Cream
                )
            }
        }
        val prog = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs else 0f
        LinearProgressIndicator(
            progress = { prog.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = MediaColors.Accent,
            trackColor = MediaColors.InkHairline
        )
    }
}

@Composable
private fun BottomBar(modifier: Modifier = Modifier, onLibraryTab: () -> Unit, onPodcastsTab: () -> Unit, onAudiobooksTab: () -> Unit) {
    Column(
        modifier.fillMaxWidth()
            .background(MediaColors.Ink)
            .border(width = 0.5.dp, color = MediaColors.InkHairline)
            .navigationBarsPadding()
    ) {
        Row(
            Modifier.fillMaxWidth().height(58.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab(Icons.Filled.Home, "Home", true) {}
            NavTab(Icons.Outlined.LibraryBooks, "Library", false) { onLibraryTab() }
            NavTab(Icons.Outlined.Podcasts, "Podcasts", false) { onPodcastsTab() }
            NavTab(Icons.Outlined.MenuBook, "Audiobooks", false) { onAudiobooksTab() }
        }
    }
}

@Composable
private fun NavTab(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val tint = if (active) MediaColors.Cream else MediaColors.CreamFaint
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@UnstableApi
@Composable
private fun FullPlayer(state: PlayerState, vm: PlayerViewModel, onClose: () -> Unit) {
    val context = LocalContext.current

    // Lightweight item to drive CoverArt from the current URI.
    val artItem = state.currentUri?.let { uri ->
        AppMediaItem(
            id = uri.substringAfterLast('/').toLongOrNull() ?: 0L,
            title = state.currentTitle, artist = state.currentArtist,
            durationMs = state.durationMs, uri = android.net.Uri.parse(uri),
            type = if (state.isVideo) MediaType.VIDEO else MediaType.AUDIO,
            pillar = Pillar.MUSIC
        )
    }

    Box(Modifier.fillMaxSize().background(MediaColors.Ink)) {
        Column(Modifier.fillMaxSize()) {
            // Top bar with close — its own row, always tappable
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(Space.sm, Space.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClose) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = MediaColors.Cream,
                        modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(if (state.isVideo) "Now playing" else "Now playing",
                    style = MaterialTheme.typography.bodyMedium, color = MediaColors.CreamFaint)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            // Hero: video surface for video, big cover art for audio
            Box(
                Modifier.fillMaxWidth().weight(1f).padding(Space.xl, Space.md),
                contentAlignment = Alignment.Center
            ) {
                if (state.isVideo) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                val token = SessionToken(ctx, ComponentName(ctx, PlaybackService::class.java))
                                val future = MediaController.Builder(ctx, token).buildAsync()
                                future.addListener({ player = future.get() }, MoreExecutors.directExecutor())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (artItem != null) {
                    CoverArt(artItem, Modifier.fillMaxWidth().aspectRatio(1f), corner = 18)
                }
            }

            // Title block — serif title, editorial
            Column(Modifier.fillMaxWidth().padding(Space.xl, 0.dp, Space.xl, Space.md)) {
                Text(state.currentTitle, style = MaterialTheme.typography.titleLarge,
                    color = MediaColors.Cream, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(state.currentArtist, style = MaterialTheme.typography.bodyLarge,
                    color = MediaColors.CreamDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // Scrubber with time labels
            Column(Modifier.fillMaxWidth().padding(Space.xl, 0.dp)) {
                if (state.durationMs > 0) {
                    Slider(
                        value = state.positionMs.toFloat().coerceIn(0f, state.durationMs.toFloat()),
                        onValueChange = { vm.seekTo(it.toLong()) },
                        valueRange = 0f..state.durationMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = MediaColors.Cream,
                            activeTrackColor = MediaColors.Accent,
                            inactiveTrackColor = MediaColors.InkHairline
                        )
                    )
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text(fmtTime(state.positionMs), style = MaterialTheme.typography.labelSmall, color = MediaColors.CreamFaint)
                        Text(fmtTime(state.durationMs), style = MaterialTheme.typography.labelSmall, color = MediaColors.CreamFaint)
                    }
                }
            }

            // Primary transport
            Row(
                Modifier.fillMaxWidth().padding(Space.xl, Space.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ vm.previous() }) { Icon(Icons.Filled.SkipPrevious, "Previous", tint = MediaColors.Cream, modifier = Modifier.size(34.dp)) }
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(MediaColors.Cream)
                        .clickable { vm.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause",
                        tint = MediaColors.OnInverse, modifier = Modifier.size(34.dp))
                }
                IconButton({ vm.next() }) { Icon(Icons.Filled.SkipNext, "Next", tint = MediaColors.Cream, modifier = Modifier.size(34.dp)) }
            }

            // Secondary row: shuffle / repeat / speed
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(Space.xl, 0.dp, Space.xl, Space.xl),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ vm.toggleShuffle() }) {
                    Icon(Icons.Filled.Shuffle, "Shuffle",
                        tint = if (state.shuffle) MediaColors.Accent else MediaColors.CreamDim,
                        modifier = Modifier.size(22.dp))
                }
                IconButton({ vm.cycleRepeat() }) {
                    Icon(
                        if (state.repeatMode == 1) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        "Repeat",
                        tint = if (state.repeatMode != 0) MediaColors.Accent else MediaColors.CreamDim,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).clickable { vm.cycleSpeed() }
                        .padding(horizontal = Space.md, vertical = Space.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${state.speed}x".replace(".0x", "x"),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.speed != 1.0f) MediaColors.Accent else MediaColors.CreamDim)
                }
            }
        }
    }
}

private fun fmtTime(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val m = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(m, sec)
}
