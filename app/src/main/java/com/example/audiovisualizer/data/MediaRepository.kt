package com.example.audiovisualizer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

class MediaRepository(private val context: Context) {

    fun scanAudioFiles(): List<MediaItem> = queryMedia(
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        isVideo = false,
    )

    fun scanVideoFiles(): List<MediaItem> = queryMedia(
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        isVideo = true,
    )

    private fun queryMedia(uri: Uri, isVideo: Boolean): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            if (isVideo) MediaStore.Video.Media.TITLE else MediaStore.Audio.Media.TITLE,
            if (isVideo) MediaStore.Video.Media.DURATION else MediaStore.Audio.Media.DURATION,
            if (isVideo) MediaStore.Video.Media.ARTIST else MediaStore.Audio.Media.ARTIST,
        )

        val items = mutableListOf<MediaItem>()
        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val titleCol = cursor.getColumnIndexOrThrow(
                if (isVideo) MediaStore.Video.Media.TITLE else MediaStore.Audio.Media.TITLE,
            )
            val durationCol = cursor.getColumnIndexOrThrow(
                if (isVideo) MediaStore.Video.Media.DURATION else MediaStore.Audio.Media.DURATION,
            )
            val artistCol = cursor.getColumnIndexOrThrow(
                if (isVideo) MediaStore.Video.Media.ARTIST else MediaStore.Audio.Media.ARTIST,
            )

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                items.add(
                    MediaItem(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol),
                        uri = contentUri,
                        durationMs = cursor.getLong(durationCol),
                        isVideo = isVideo,
                    ),
                )
            }
        }
        return items
    }
}
