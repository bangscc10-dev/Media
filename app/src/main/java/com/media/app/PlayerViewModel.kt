package com.media.app

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerState(
    val currentTitle: String = "",
    val currentArtist: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasItem: Boolean = false
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = refresh()
        override fun onMediaItemTransition(mediaItem: ExoMediaItem?, reason: Int) = refresh()
        override fun onPlaybackStateChanged(playbackState: Int) = refresh()
    }

    init {
        val token = SessionToken(
            app,
            ComponentName(app, PlaybackService::class.java)
        )
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            refresh()
            startPositionUpdates()
        }, MoreExecutors.directExecutor())
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                controller?.let {
                    if (it.isPlaying) {
                        _state.value = _state.value.copy(
                            positionMs = it.currentPosition,
                            durationMs = it.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun refresh() {
        val c = controller ?: return
        val md = c.mediaMetadata
        _state.value = PlayerState(
            currentTitle = md.title?.toString() ?: "",
            currentArtist = md.artist?.toString() ?: "",
            isPlaying = c.isPlaying,
            positionMs = c.currentPosition,
            durationMs = c.duration.coerceAtLeast(0L),
            hasItem = c.currentMediaItem != null
        )
    }

    fun play(items: List<AppMediaItem>, startIndex: Int) {
        val c = controller ?: return
        val exoItems = items.map { item ->
            ExoMediaItem.Builder()
                .setUri(item.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist)
                        .build()
                )
                .build()
        }
        c.setMediaItems(exoItems, startIndex, 0L)
        c.prepare()
        c.play()
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun seekTo(ms: Long) { controller?.seekTo(ms) }
    fun next() { controller?.seekToNext() }
    fun previous() { controller?.seekToPrevious() }

    override fun onCleared() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        super.onCleared()
    }
}
