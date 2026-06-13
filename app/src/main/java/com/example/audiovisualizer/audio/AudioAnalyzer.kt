package com.example.audiovisualizer.audio

import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.hypot
import kotlin.math.roundToInt

data class AudioFrame(
    val magnitudes: FloatArray = FloatArray(64),
    val waveform: FloatArray = FloatArray(128),
    val sequence: Long = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioFrame) return false
        return sequence == other.sequence
    }

    override fun hashCode(): Int = sequence.hashCode()
}

class AudioAnalyzer {

    private var visualizer: Visualizer? = null
    private var audioSessionId: Int = 0
    private var captureRateFraction = 0.75f
    private var frameSequence = 0L

    private val _audioFrame = MutableStateFlow(AudioFrame())
    val audioFrame: StateFlow<AudioFrame> = _audioFrame.asStateFlow()

    private var smoothedMagnitudes = FloatArray(64)
    private var latestWaveform = FloatArray(128)
    private var smoothing = 0.65f
    private val pendingFrame = AtomicReference<AudioFrame?>(null)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun setSmoothing(value: Float) {
        smoothing = value.coerceIn(0f, 0.95f)
    }

    fun setUpdateFrequency(fraction: Float) {
        val clamped = fraction.coerceIn(0.25f, 1f)
        if (clamped == captureRateFraction) return
        captureRateFraction = clamped
        if (audioSessionId > 0) {
            attachToSession(audioSessionId, force = true)
        }
    }

    fun attachToSession(sessionId: Int, force: Boolean = false) {
        if (sessionId <= 0) return
        if (!force && sessionId == audioSessionId && visualizer != null) return

        release()
        audioSessionId = sessionId
        smoothedMagnitudes = FloatArray(64)

        try {
            val captureSize = Visualizer.getCaptureSizeRange()[1]
            val captureRate = computeCaptureRate()
            visualizer = Visualizer(sessionId).apply {
                this.captureSize = captureSize
                scalingMode = Visualizer.SCALING_MODE_NORMALIZED
                measurementMode = Visualizer.MEASUREMENT_MODE_PEAK_RMS

                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int,
                        ) {
                            if (waveform == null) return
                            latestWaveform = FftDecoder.toWaveform(waveform)
                            publishFrame(latestWaveform)
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int,
                        ) {
                            if (fft == null) return
                            val raw = FftDecoder.toMagnitudes(fft)
                            for (i in raw.indices) {
                                smoothedMagnitudes[i] =
                                    smoothedMagnitudes[i] * smoothing + raw[i] * (1f - smoothing)
                            }
                            publishFrame(latestWaveform)
                        }
                    },
                    captureRate,
                    true,
                    true,
                )
                enabled = true
            }
        } catch (_: Exception) {
            release()
        }
    }

    private fun publishFrame(waveform: FloatArray) {
        frameSequence += 1
        val frame = AudioFrame(
            magnitudes = smoothedMagnitudes.copyOf(),
            waveform = waveform.copyOf(),
            sequence = frameSequence,
        )
        pendingFrame.set(frame)
        mainHandler.post {
            val latest = pendingFrame.getAndSet(null) ?: return@post
            _audioFrame.value = latest
        }
    }

    fun getEstimatedCaptureRateHz(): Int = computeCaptureRate() / 1000

    private fun computeCaptureRate(): Int {
        val maxRate = Visualizer.getMaxCaptureRate().coerceAtLeast(1000)
        return (maxRate * captureRateFraction).roundToInt().coerceIn(1000, maxRate)
    }

    fun release() {
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
    }

    fun detach() {
        release()
        audioSessionId = 0
    }
}

private object FftDecoder {
    fun toMagnitudes(fft: ByteArray): FloatArray {
        val binCount = minOf(64, fft.size / 2)
        val magnitudes = FloatArray(64)
        for (i in 0 until binCount) {
            val real = fft[i * 2].toFloat()
            val imag = fft[i * 2 + 1].toFloat()
            val magnitude = hypot(real.toDouble(), imag.toDouble()).toFloat()
            magnitudes[i] = (magnitude / 64f).coerceIn(0f, 1f)
        }
        return magnitudes
    }

    fun toWaveform(waveform: ByteArray): FloatArray {
        val floats = FloatArray(waveform.size)
        for (i in waveform.indices) {
            floats[i] = (waveform[i].toInt() and 0xFF) / 128f - 1f
        }
        return floats
    }
}
