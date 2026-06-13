package com.example.audiovisualizer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.audiovisualizer.R
import com.example.audiovisualizer.audio.PlaybackUiState
import com.example.audiovisualizer.data.MediaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryPanelContent(
    uiState: PlaybackUiState,
    onScan: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val hasPermission = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.all { it }) onScan()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && uiState.audioFiles.isEmpty() && uiState.videoFiles.isEmpty() && !uiState.isScanning) {
            onScan()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.library), color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (hasPermission) onScan() else launcher.launch(permissions)
                    },
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.scan_media), tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        if (!hasPermission) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.permission_required), color = Color.White)
                    Button(
                        onClick = { launcher.launch(permissions) },
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
            return@Column
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("${stringResource(R.string.audio_files)} (${uiState.audioFiles.size})") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("${stringResource(R.string.video_files)} (${uiState.videoFiles.size})") },
            )
        }

        if (uiState.isScanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val items = if (selectedTab == 0) uiState.audioFiles else uiState.videoFiles
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.no_media_found), color = Color.White)
                        Button(onClick = onScan, modifier = Modifier.padding(top = 16.dp)) {
                            Text(stringResource(R.string.scan_media))
                        }
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(items, key = { "${it.isVideo}-${it.id}" }) { item ->
                        MediaListItem(
                            item = item,
                            isPlaying = uiState.currentTrack?.id == item.id && uiState.isPlaying,
                            onClick = {
                                onPlay(item)
                                onClose()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaListItem(
    item: MediaItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.White,
            )
        },
        supportingContent = {
            val artist = item.artist ?: "Unknown"
            val duration = formatDuration(item.durationMs)
            Text("$artist · $duration", maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(0.7f))
        },
        leadingContent = {
            Icon(
                imageVector = if (item.isVideo) Icons.Default.VideoFile else Icons.Default.AudioFile,
                contentDescription = null,
                tint = Color.White.copy(0.8f),
            )
        },
    )
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "--:--"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
