package com.example.audiovisualizer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audiovisualizer.R
import com.example.audiovisualizer.audio.PlaybackUiState
import com.example.audiovisualizer.data.MediaItem
import com.example.audiovisualizer.ui.components.PanelSide
import com.example.audiovisualizer.ui.components.SideOverlayPanel
import com.example.audiovisualizer.visualizer.CircularVisualizer
import kotlinx.coroutines.flow.StateFlow
import com.example.audiovisualizer.audio.AudioFrame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uiState: PlaybackUiState,
    audioFrameFlow: StateFlow<AudioFrame>,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onScan: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    onUpdateSettings: ((com.example.audiovisualizer.data.VisualizerSettings) -> com.example.audiovisualizer.data.VisualizerSettings) -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var libraryOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { libraryOpen = true }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.library), tint = Color.White)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = uiState.currentTrack?.title ?: stringResource(R.string.now_playing),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                            )
                            Text(
                                text = uiState.currentTrack?.artist ?: stringResource(R.string.select_track),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { settingsOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.settings), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x88000000)),
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                CircularVisualizer(
                    audioFrameFlow = audioFrameFlow,
                    settings = uiState.settings,
                    modifier = Modifier.fillMaxSize(),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (uiState.durationMs > 0) {
                        Slider(
                            value = uiState.positionMs.toFloat(),
                            onValueChange = { onSeek(it.toLong()) },
                            valueRange = 0f..uiState.durationMs.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "${formatTime(uiState.positionMs)} / ${formatTime(uiState.durationMs)}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { onSeek(0) }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White)
                        }
                        IconButton(onClick = onTogglePlayPause) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                        IconButton(onClick = { onSeek(uiState.durationMs) }) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        SideOverlayPanel(
            visible = libraryOpen,
            side = PanelSide.Left,
            onDismiss = { libraryOpen = false },
        ) {
            LibraryPanelContent(
                uiState = uiState,
                onScan = onScan,
                onPlay = onPlay,
                onClose = { libraryOpen = false },
            )
        }

        SideOverlayPanel(
            visible = settingsOpen,
            side = PanelSide.Right,
            onDismiss = { settingsOpen = false },
        ) {
            SettingsPanelContent(
                settings = uiState.settings,
                captureRateHz = uiState.captureRateHz,
                onUpdate = onUpdateSettings,
                onReset = onResetSettings,
                onClose = { settingsOpen = false },
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
