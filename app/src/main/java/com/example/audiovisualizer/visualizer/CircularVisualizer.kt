package com.example.audiovisualizer.visualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.audiovisualizer.data.VisualizerSettings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

@Composable
fun CircularVisualizer(
    audioFrameFlow: StateFlow<com.example.audiovisualizer.audio.AudioFrame>,
    settings: VisualizerSettings,
    modifier: Modifier = Modifier,
) {
    var magnitudes by remember { mutableStateOf(FloatArray(64)) }
    var waveform by remember { mutableStateOf(FloatArray(128)) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var time by remember { mutableFloatStateOf(0f) }
    var peaks by remember { mutableStateOf(FloatArray(settings.barCount)) }
    var frameTick by remember { mutableLongStateOf(0L) }
    val peakState = remember { PeakHoldState(settings.barCount) }
    val currentSettings by rememberUpdatedState(settings)

    LaunchedEffect(audioFrameFlow) {
        audioFrameFlow.collectLatest { frame ->
            magnitudes = frame.magnitudes
            waveform = frame.waveform
        }
    }

    LaunchedEffect(settings.barCount) {
        peakState.resize(settings.barCount)
        peaks = FloatArray(settings.barCount)
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = (nanos - lastNanos) / 1_000_000_000f
                    val s = currentSettings
                    rotation += s.rotationSpeed * dt
                    time += dt
                    peaks = peakState.update(
                        magnitudes,
                        s.peakFallSpeed,
                        s.peakHoldEnabled,
                    )
                }
                lastNanos = nanos
                frameTick = nanos
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A0A2E),
                        Color(0xFF0D0221),
                        Color(0xFF05010A),
                    ),
                ),
            ),
    ) {
        @Suppress("UNUSED_VARIABLE")
        val invalidate = frameTick

        val glowAlpha = settings.glowIntensity * 0.15f
        if (glowAlpha > 0.01f) {
            drawCircle(
                brush = VisualizerColors.radialBrush(
                    settings.colorScheme,
                    center = center,
                    radius = size.minDimension / 2f,
                    alpha = glowAlpha,
                ),
                radius = size.minDimension / 2f * settings.outerScale,
                center = center,
            )
        }

        VisualizerRenderers.draw(
            scope = this,
            type = settings.visualizerType,
            magnitudes = magnitudes,
            waveform = waveform,
            settings = settings,
            rotation = rotation,
            peaks = peaks,
            time = time,
        )
    }
}
