package com.example.audiovisualizer.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.audiovisualizer.data.ColorScheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object VisualizerColors {

    fun gradientColor(scheme: ColorScheme, t: Float, alpha: Float = 1f): Color {
        val colors = schemeColors(scheme)
        val scaled = (t.coerceIn(0f, 1f) * (colors.size - 1))
        val index = scaled.toInt().coerceIn(0, colors.size - 2)
        val fraction = scaled - index
        return lerp(colors[index], colors[index + 1], fraction).copy(alpha = alpha)
    }

    fun radialBrush(
        scheme: ColorScheme,
        center: Offset,
        radius: Float,
        alpha: Float = 1f,
    ): Brush = Brush.radialGradient(
        colors = schemeColors(scheme).map { it.copy(alpha = alpha) },
        center = center,
        radius = radius,
    )

    fun sweepBrush(
        scheme: ColorScheme,
        center: Offset,
        radius: Float,
        alpha: Float = 1f,
    ): Brush = Brush.sweepGradient(
        colors = schemeColors(scheme).map { it.copy(alpha = alpha) },
        center = center,
    )

    private fun schemeColors(scheme: ColorScheme): List<Color> = when (scheme) {
        ColorScheme.NeonMagenta -> listOf(
            Color(0xFFFF006E),
            Color(0xFFE040FB),
            Color(0xFFBB86FC),
            Color(0xFFFF6090),
        )
        ColorScheme.OceanCyan -> listOf(
            Color(0xFF001845),
            Color(0xFF0077B6),
            Color(0xFF00B4D8),
            Color(0xFF90E0EF),
        )
        ColorScheme.Sunset -> listOf(
            Color(0xFFFF6B35),
            Color(0xFFFFB627),
            Color(0xFFFF006E),
            Color(0xFF7B2CBF),
        )
        ColorScheme.LimeBlue -> listOf(
            Color(0xFF39FF14),
            Color(0xFF00E676),
            Color(0xFF00B0FF),
            Color(0xFF304FFE),
        )
        ColorScheme.PurpleHaze -> listOf(
            Color(0xFF4A0E78),
            Color(0xFF7B2CBF),
            Color(0xFF9D4EDD),
            Color(0xFFE0AAFF),
        )
        ColorScheme.Spectrum -> listOf(
            Color(0xFFFF006E),
            Color(0xFFFFBE0B),
            Color(0xFF39FF14),
            Color(0xFF00B4D8),
            Color(0xFF7B2CBF),
            Color(0xFFFF006E),
        )
    }

    private fun lerp(a: Color, b: Color, t: Float): Color = Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t,
    )
}

class PeakHoldState(private var size: Int) {
    private var peaks = FloatArray(size)

    fun update(values: FloatArray, fallSpeed: Float, enabled: Boolean): FloatArray {
        if (values.size != peaks.size) {
            peaks = FloatArray(values.size)
            size = values.size
        }
        val result = FloatArray(values.size)
        for (i in values.indices) {
            if (enabled) {
                peaks[i] = maxOf(values[i], peaks[i] - fallSpeed)
                result[i] = peaks[i]
            } else {
                peaks[i] = values[i]
                result[i] = values[i]
            }
        }
        return result
    }

    fun resize(newSize: Int) {
        if (newSize != size) {
            peaks = FloatArray(newSize)
            size = newSize
        }
    }
}

fun angleForIndex(index: Int, count: Int, rotation: Float = 0f): Float =
    (index.toFloat() / count * 2f * PI.toFloat()) + rotation - PI.toFloat() / 2f

fun magnitudeAt(magnitudes: FloatArray, index: Int, count: Int, sensitivity: Float): Float {
    if (magnitudes.isEmpty()) return 0f
    // Log-spaced frequency mapping: low bins = bass, high bins = treble
    val t = index.toFloat() / count.coerceAtLeast(1)
    val srcIndex = (t * t * (magnitudes.size - 1)).toInt().coerceIn(0, magnitudes.size - 1)
    return (magnitudes[srcIndex] * sensitivity).coerceIn(0f, 1f)
}

fun waveformAt(waveform: FloatArray, index: Int, count: Int): Float {
    val srcIndex = (index.toFloat() / count * waveform.size).toInt()
        .coerceIn(0, waveform.size - 1)
    return waveform[srcIndex]
}

fun polarToOffset(cx: Float, cy: Float, radius: Float, angle: Float): Offset =
    Offset(cx + radius * cos(angle), cy + radius * sin(angle))
