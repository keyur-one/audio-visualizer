package com.example.audiovisualizer.audio

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.example.audiovisualizer.data.MediaItem
import com.example.audiovisualizer.data.MediaRepository
import com.example.audiovisualizer.data.SettingsRepository
import com.example.audiovisualizer.data.VisualizerSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val currentTrack: MediaItem? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isServiceReady: Boolean = false,
    val settings: VisualizerSettings = VisualizerSettings.Default,
    val audioFiles: List<MediaItem> = emptyList(),
    val videoFiles: List<MediaItem> = emptyList(),
    val isScanning: Boolean = false,
    val captureRateHz: Int = 0,
)

class PlaybackViewModel(
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val _audioFrame = MutableStateFlow(AudioFrame())
    val audioFrame: StateFlow<AudioFrame> = _audioFrame.asStateFlow()

    private var playerListener: Player.Listener? = null
    private var audioFrameJob: Job? = null
    private var settingsJob: Job? = null
    private var positionJob: Job? = null

    fun startService(context: Context) {
        val intent = Intent(context, PlaybackService::class.java)
        context.startService(intent)
        viewModelScope.launch {
            repeat(20) {
                val service = PlaybackService.instance
                if (service != null) {
                    observeService(service)
                    return@launch
                }
                delay(100)
            }
        }
    }

    private fun observeService(service: PlaybackService) {
        val player = service.getPlayer() ?: return

        playerListener?.let { player.removeListener(it) }
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePosition(player)
            }
        }
        playerListener = listener
        player.addListener(listener)

        audioFrameJob?.cancel()
        audioFrameJob = viewModelScope.launch {
            service.getAnalyzer().audioFrame.collect { frame ->
                _audioFrame.value = frame
            }
        }

        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            settingsRepository.settings.collectLatest { settings ->
                val analyzer = service.getAnalyzer()
                analyzer.setSmoothing(settings.smoothing)
                analyzer.setUpdateFrequency(settings.updateFrequency)
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    captureRateHz = analyzer.getEstimatedCaptureRateHz(),
                )
            }
        }

        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (isActive) {
                updatePosition(player)
                delay(200)
            }
        }

        _uiState.value = _uiState.value.copy(
            isServiceReady = true,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition,
            durationMs = player.duration.coerceAtLeast(0L),
        )
    }

    private fun updatePosition(player: Player) {
        _uiState.value = _uiState.value.copy(
            positionMs = player.currentPosition,
            durationMs = player.duration.coerceAtLeast(0L),
        )
    }

    fun playTrack(item: MediaItem) {
        PlaybackService.instance?.playMedia(item)
        _uiState.value = _uiState.value.copy(currentTrack = item)
    }

    fun togglePlayPause() {
        val player = PlaybackService.instance?.getPlayer() ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        PlaybackService.instance?.getPlayer()?.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(positionMs = positionMs)
    }

    fun scanMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val audio = mediaRepository.scanAudioFiles()
            val video = mediaRepository.scanVideoFiles()
            _uiState.value = _uiState.value.copy(
                audioFiles = audio,
                videoFiles = video,
                isScanning = false,
            )
        }
    }

    fun updateSettings(transform: (VisualizerSettings) -> VisualizerSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }

    override fun onCleared() {
        audioFrameJob?.cancel()
        settingsJob?.cancel()
        positionJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val mediaRepository: MediaRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaybackViewModel(mediaRepository, settingsRepository) as T
        }
    }
}
