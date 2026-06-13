package com.example.audiovisualizer.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val title: String,
    val artist: String?,
    val uri: Uri,
    val durationMs: Long,
    val isVideo: Boolean,
)
