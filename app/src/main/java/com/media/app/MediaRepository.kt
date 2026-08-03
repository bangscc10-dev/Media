package com.media.app

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

enum class MediaType { AUDIO, VIDEO }

data class AppMediaItem(
    val id: Long,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val uri: Uri,
    val type: MediaType
)

object MediaRepository {

    fun loadAudio(context: Context): List<AppMediaItem> =
        query(
            context,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaType.AUDIO,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            ),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        )

    fun loadVideo(context: Context): List<AppMediaItem> =
        query(
            context,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaType.VIDEO,
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.DURATION
            ),
            null
        )

    private fun query(
        context: Context,
        collection: Uri,
        type: MediaType,
        projection: Array<String>,
        selection: String?
    ): List<AppMediaItem> {
        val items = mutableListOf<AppMediaItem>()
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${projection[1]} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(projection[0])
            val titleCol = cursor.getColumnIndexOrThrow(projection[1])
            val artistCol = cursor.getColumnIndexOrThrow(projection[2])
            val durCol = cursor.getColumnIndexOrThrow(projection[3])
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                items += AppMediaItem(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Unknown",
                    artist = cursor.getString(artistCol) ?: "Unknown",
                    durationMs = cursor.getLong(durCol),
                    uri = Uri.withAppendedPath(collection, id.toString()),
                    type = type
                )
            }
        }
        return items
    }
}
