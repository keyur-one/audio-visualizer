package com.example.audiovisualizer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.audiovisualizer.audio.PlaybackViewModel
import com.example.audiovisualizer.ui.screens.PlayerScreen

@Composable
fun AudioVisualizerApp(viewModel: PlaybackViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    PlayerScreen(
        uiState = uiState,
        audioFrameFlow = viewModel.audioFrame,
        onTogglePlayPause = viewModel::togglePlayPause,
        onSeek = viewModel::seekTo,
        onScan = viewModel::scanMedia,
        onPlay = viewModel::playTrack,
        onUpdateSettings = viewModel::updateSettings,
        onResetSettings = viewModel::resetSettings,
    )
}
