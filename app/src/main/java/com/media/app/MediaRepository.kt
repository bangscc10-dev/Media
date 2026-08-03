package com.media.app

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

enum class MediaType { AUDIO, VIDEO }

enum class Pillar { MUSIC, PODCAST, AUDIOBOOK, VIDEO }

data class AppMediaItem(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uri: Uri,
    val type: MediaType,
    val pillar: Pillar,
    val details: String? = null
)

object MediaRepository {

    private const val TEN_MIN_MS = 10 * 60 * 1000L

    private fun classifyAudio(title: String, relPath: String, durationMs: Long): Pillar {
        val path = relPath.lowercase()
        return when {
            path.contains("audiobooks/") -> Pillar.AUDIOBOOK
            path.contains("podcasts/") -> Pillar.PODCAST
            path.contains("music/") -> Pillar.MUSIC
            title.lowercase().contains("podcast") -> Pillar.PODCAST
            durationMs > TEN_MIN_MS -> Pillar.PODCAST
            else -> Pillar.MUSIC
        }
    }

    private var rawAudio: List<AppMediaItem>? = null
    private var videoCache: List<AppMediaItem>? = null

    fun refresh() { rawAudio = null; videoCache = null }

    // Raw audio with heuristic classification, no overrides applied.
    private fun rawAudio(context: Context): List<AppMediaItem> {
        rawAudio?.let { return it }
        val items = mutableListOf<AppMediaItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.RELATIVE_PATH
        )
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val title = c.getString(titleCol) ?: "Unknown"
                val dur = c.getLong(durCol)
                val relPath = c.getString(pathCol) ?: ""
                items += AppMediaItem(
                    id = id, title = title,
                    artist = c.getString(artistCol) ?: "Unknown",
                    durationMs = dur,
                    uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString()),
                    type = MediaType.AUDIO,
                    pillar = classifyAudio(title, relPath, dur)
                )
            }
        }
        rawAudio = items
        return items
    }

    // Apply user overrides on top of the raw item.
    private fun applyOverride(item: AppMediaItem, ov: MediaOverride?): AppMediaItem {
        if (ov == null) return item
        val newPillar = when (ov.pillar) {
            "MUSIC" -> Pillar.MUSIC
            "PODCAST" -> Pillar.PODCAST
            "AUDIOBOOK" -> Pillar.AUDIOBOOK
            else -> item.pillar
        }
        return item.copy(
            title = ov.customTitle?.takeIf { it.isNotBlank() } ?: item.title,
            artist = ov.customArtist?.takeIf { it.isNotBlank() } ?: item.artist,
            pillar = newPillar,
            details = ov.details?.takeIf { it.isNotBlank() }
        )
    }

    fun audioWithOverrides(context: Context, overrides: Map<Long, MediaOverride>): List<AppMediaItem> =
        rawAudio(context).map { applyOverride(it, overrides[it.id]) }

    fun loadVideo(context: Context): List<AppMediaItem> {
        videoCache?.let { return it }
        val items = mutableListOf<AppMediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DURATION
        )
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            "${MediaStore.Video.Media.TITLE} ASC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                items += AppMediaItem(
                    id = id,
                    title = c.getString(titleCol) ?: "Unknown",
                    artist = c.getString(artistCol) ?: "Unknown",
                    durationMs = c.getLong(durCol),
                    uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString()),
                    type = MediaType.VIDEO,
                    pillar = Pillar.VIDEO
                )
            }
        }
        videoCache = items
        return items
    }
}
