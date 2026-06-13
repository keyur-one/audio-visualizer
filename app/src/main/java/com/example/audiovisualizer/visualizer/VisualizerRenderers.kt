package com.example.audiovisualizer.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.audiovisualizer.data.VisualizerSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object VisualizerRenderers {

    fun draw(
        scope: DrawScope,
        type: VisualizerType,
        magnitudes: FloatArray,
        waveform: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        peaks: FloatArray,
        time: Float,
    ) {
        with(scope) {
            when (type) {
                VisualizerType.ConcentricBarRing -> drawConcentricBarRing(
                    magnitudes, settings, rotation, peaks,
                )
                VisualizerType.OscillatingRibbon -> drawOscillatingRibbon(
                    magnitudes, waveform, settings, rotation, time,
                )
                VisualizerType.RadialDottedBars -> drawRadialDottedBars(
                    magnitudes, settings, rotation, peaks,
                )
                VisualizerType.WavyParticlePath -> drawWavyParticlePath(
                    magnitudes, waveform, settings, rotation, time,
                )
                VisualizerType.ClassicRadialLines -> drawClassicRadialLines(
                    magnitudes, settings, rotation, peaks,
                )
                VisualizerType.FluidWaveRibbon -> drawFluidWaveRibbon(
                    magnitudes, waveform, settings, rotation, time,
                )
                VisualizerType.SwirlingHairyCircle -> drawSwirlingHairyCircle(
                    magnitudes, settings, rotation, time,
                )
                VisualizerType.ParallelLineSphere -> drawParallelLineSphere(
                    magnitudes, settings, rotation, time,
                )
                VisualizerType.DenseRadialSpikes -> drawDenseRadialSpikes(
                    magnitudes, settings, rotation, peaks,
                )
                VisualizerType.CircularSpectrumPeaks -> drawCircularSpectrumPeaks(
                    magnitudes, settings, rotation, peaks,
                )
            }
        }
    }

    private fun DrawScope.dimensions(settings: VisualizerSettings): Dimensions {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = minOf(size.width, size.height) / 2f * settings.outerScale
        val innerR = maxR * settings.innerRadius
        return Dimensions(cx, cy, maxR, innerR)
    }

    private data class Dimensions(val cx: Float, val cy: Float, val maxR: Float, val innerR: Float)

    private fun DrawScope.drawConcentricBarRing(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        peaks: FloatArray,
    ) {
        val d = dimensions(settings)
        val count = settings.barCount
        val layers = 3
        val barW = settings.thickness * 2.5f

        for (layer in 0 until layers) {
            val layerScale = 1f - layer * 0.12f
            for (i in 0 until count) {
                val angle = angleForIndex(i, count, rotation + layer * 0.05f)
                val mag = magnitudeAt(magnitudes, i, count, settings.sensitivity) * layerScale
                val peak = peaks.getOrElse(i) { mag }
                val extent = maxOf(mag, peak * 0.7f, 0.05f)
                val barLen = (d.maxR - d.innerR) * 0.35f * extent
                val r0 = d.innerR + (d.maxR - d.innerR) * layer * 0.28f
                val start = polarToOffset(d.cx, d.cy, r0, angle)
                val end = polarToOffset(d.cx, d.cy, r0 + barLen, angle)

                val colorT = i.toFloat() / count
                val color = VisualizerColors.gradientColor(settings.colorScheme, colorT, 0.9f)
                drawLine(
                    color = color,
                    start = start,
                    end = end,
                    strokeWidth = barW,
                    cap = StrokeCap.Round,
                )
            }
        }
    }

    private fun DrawScope.drawOscillatingRibbon(
        magnitudes: FloatArray,
        waveform: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        time: Float,
    ) {
        val d = dimensions(settings)
        val layers = 6
        val points = settings.barCount.coerceAtLeast(48)

        for (layer in 0 until layers) {
            val path = Path()
            val layerOffset = layer * 0.08f
            for (i in 0..points) {
                val t = i.toFloat() / points
                val angle = t * 2f * PI.toFloat() + rotation + layerOffset
                val mag = magnitudeAt(magnitudes, i, points, settings.sensitivity)
                val wave = waveformAt(waveform, i, points)
                val wobble = sin(angle * 3f + time * 2f + layer) * 0.08f
                val r = d.innerR + (d.maxR - d.innerR) * (0.4f + mag * 0.5f + wave * 0.15f + wobble)
                val pt = polarToOffset(d.cx, d.cy, r, angle)
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
            val alpha = 0.35f + layer * 0.1f
            drawPath(
                path = path,
                color = VisualizerColors.gradientColor(settings.colorScheme, layer / layers.toFloat(), alpha),
                style = Stroke(width = settings.thickness, cap = StrokeCap.Round),
            )
        }
    }

    private fun DrawScope.drawRadialDottedBars(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        peaks: FloatArray,
    ) {
        val d = dimensions(settings)
        val count = settings.barCount
        val dotSize = settings.thickness * 1.8f

        for (i in 0 until count) {
            val angle = angleForIndex(i, count, rotation)
            val mag = magnitudeAt(magnitudes, i, count, settings.sensitivity)
            val peak = peaks.getOrElse(i) { mag }
            val dots = (mag * 12).toInt().coerceAtLeast(1)
            val peakDots = (peak * 12).toInt()

            for (dot in 0 until maxOf(dots, peakDots)) {
                val t = dot / 12f
                val r = d.innerR + (d.maxR - d.innerR) * t
                val alpha = if (dot < dots) 0.9f else 0.4f
                val center = polarToOffset(d.cx, d.cy, r, angle)
                val colorT = t
                drawRect(
                    color = VisualizerColors.gradientColor(settings.colorScheme, colorT, alpha),
                    topLeft = Offset(center.x - dotSize / 2, center.y - dotSize / 2),
                    size = Size(dotSize, dotSize),
                )
            }
        }
    }

    private fun DrawScope.drawWavyParticlePath(
        magnitudes: FloatArray,
        waveform: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        time: Float,
    ) {
        val d = dimensions(settings)
        val points = settings.barCount.coerceAtLeast(64)
        val dotR = settings.thickness * 1.2f

        for (i in 0 until points) {
            val t = i.toFloat() / points
            val angle = t * 2f * PI.toFloat() + rotation
            val mag = magnitudeAt(magnitudes, i, points, settings.sensitivity)
            val wave = waveformAt(waveform, i, points)
            val wobble = sin(angle * 5f + time * 3f) * 0.12f * (1f + mag)
            val r = d.innerR + (d.maxR - d.innerR) * (0.55f + mag * 0.35f + wave * 0.1f + wobble)
            val center = polarToOffset(d.cx, d.cy, r, angle)
            drawCircle(
                color = VisualizerColors.gradientColor(settings.colorScheme, t, 0.85f),
                radius = dotR * (0.8f + mag * 0.6f),
                center = center,
            )
        }
    }

    private fun DrawScope.drawClassicRadialLines(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        peaks: FloatArray,
    ) {
        val d = dimensions(settings)
        val count = settings.barCount

        for (i in 0 until count) {
            val angle = angleForIndex(i, count, rotation)
            val mag = magnitudeAt(magnitudes, i, count, settings.sensitivity)
            val extent = mag.coerceAtLeast(0.04f)
            val start = polarToOffset(d.cx, d.cy, d.innerR, angle)
            val end = polarToOffset(
                d.cx, d.cy,
                d.innerR + (d.maxR - d.innerR) * extent,
                angle,
            )
            drawLine(
                color = VisualizerColors.gradientColor(settings.colorScheme, i.toFloat() / count),
                start = start,
                end = end,
                strokeWidth = settings.thickness,
                cap = StrokeCap.Round,
            )
        }
    }

    private fun DrawScope.drawFluidWaveRibbon(
        magnitudes: FloatArray,
        waveform: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        time: Float,
    ) {
        val d = dimensions(settings)
        val ribbons = 4
        val points = settings.barCount.coerceAtLeast(72)

        for (ribbon in 0 until ribbons) {
            val path = Path()
            val phase = ribbon * PI.toFloat() / 2f
            for (i in 0..points) {
                val t = i.toFloat() / points
                val angle = t * 2f * PI.toFloat() + rotation
                val mag = magnitudeAt(magnitudes, i, points, settings.sensitivity * 0.8f)
                val wave = waveformAt(waveform, i, points)
                val fluid = sin(angle * 2f + time + phase) * 0.1f
                val r = d.innerR + (d.maxR - d.innerR) * (0.35f + ribbon * 0.12f + mag * 0.4f + wave * 0.12f + fluid)
                val pt = polarToOffset(d.cx, d.cy, r, angle)
                if (i == 0) path.moveTo(pt.x, pt.y) else {
                    val prevT = (i - 1).toFloat() / points
                    val prevAngle = prevT * 2f * PI.toFloat() + rotation
                    val midAngle = (prevAngle + angle) / 2f
                    val midR = r * 0.98f
                    val mid = polarToOffset(d.cx, d.cy, midR, midAngle)
                    path.quadraticTo(mid.x, mid.y, pt.x, pt.y)
                }
            }
            drawPath(
                path = path,
                brush = VisualizerColors.sweepBrush(settings.colorScheme, Offset(d.cx, d.cy), d.maxR, 0.5f + ribbon * 0.12f),
                style = Stroke(width = settings.thickness * 1.5f, cap = StrokeCap.Round),
            )
        }
    }

    private fun DrawScope.drawSwirlingHairyCircle(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        time: Float,
    ) {
        val d = dimensions(settings)
        val count = settings.barCount * 2
        val hairLen = (d.maxR - d.innerR) * 0.25f

        for (i in 0 until count) {
            val angle = angleForIndex(i, count, rotation + sin(time + i * 0.1f) * 0.05f)
            val mag = magnitudeAt(magnitudes, i, count, settings.sensitivity)
            val start = polarToOffset(d.cx, d.cy, d.innerR + (d.maxR - d.innerR) * 0.3f, angle)
            val curl = angle + PI.toFloat() / 3f * mag + sin(time * 2f + i) * 0.3f
            val end = polarToOffset(
                start.x, start.y,
                hairLen * (0.5f + mag),
                curl,
            )
            drawLine(
                color = VisualizerColors.gradientColor(settings.colorScheme, mag, 0.7f),
                start = start,
                end = end,
                strokeWidth = settings.thickness * 0.8f,
                cap = StrokeCap.Round,
            )
        }
    }

    private fun DrawScope.drawParallelLineSphere(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        time: Float,
    ) {
        val d = dimensions(settings)
        val lines = settings.barCount
        val avgMag = magnitudes.average().toFloat() * settings.sensitivity

        rotate(rotation * 57.3f, Offset(d.cx, d.cy)) {
            for (i in 0 until lines) {
                val t = i.toFloat() / lines
                val y = d.cy - d.maxR + t * d.maxR * 2f
                val dx = kotlin.math.sqrt((d.maxR * d.maxR - (y - d.cy) * (y - d.cy)).coerceAtLeast(0f))
                val wave = sin(t * PI.toFloat() * 4f + rotation * 2f + time * 1.5f) * (0.08f + 0.15f * avgMag)
                val x1 = d.cx - dx * (0.7f + wave)
                val x2 = d.cx + dx * (0.7f + wave)
                val colorT = t
                drawLine(
                    color = VisualizerColors.gradientColor(settings.colorScheme, colorT, 0.75f),
                    start = Offset(x1, y),
                    end = Offset(x2, y),
                    strokeWidth = settings.thickness * 0.9f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }

    private fun DrawScope.drawDenseRadialSpikes(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        peaks: FloatArray,
    ) {
        val d = dimensions(settings)
        val count = settings.barCount * 2

        for (i in 0 until count) {
            val angle = angleForIndex(i, count, rotation)
            val mag = magnitudeAt(magnitudes, i, count, settings.sensitivity)
            val extent = mag.coerceAtLeast(0.04f)
            val start = polarToOffset(d.cx, d.cy, d.innerR * 0.9f, angle)
            val end = polarToOffset(
                d.cx, d.cy,
                d.innerR + (d.maxR - d.innerR) * extent * 0.95f,
                angle,
            )
            drawLine(
                color = VisualizerColors.gradientColor(settings.colorScheme, 1f - mag, 0.85f),
                start = start,
                end = end,
                strokeWidth = settings.thickness * 0.6f,
                cap = StrokeCap.Round,
            )
        }
    }

    private fun DrawScope.drawCircularSpectrumPeaks(
        magnitudes: FloatArray,
        settings: VisualizerSettings,
        rotation: Float,
        peaks: FloatArray,
    ) {
        val d = dimensions(settings)
        val count = settings.barCount
        val barWidth = (2f * PI.toFloat() * d.innerR / count) * 0.7f

        for (i in 0 until count) {
            val angle = angleForIndex(i, count, rotation)
            val mag = magnitudeAt(magnitudes, i, count, settings.sensitivity)
            val peak = peaks.getOrElse(i) { mag }
            val extent = mag.coerceAtLeast(0.04f)

            val barStart = d.innerR
            val barEnd = d.innerR + (d.maxR - d.innerR) * extent
            val start = polarToOffset(d.cx, d.cy, barStart, angle)
            val end = polarToOffset(d.cx, d.cy, barEnd, angle)

            val colorT = mag
            drawLine(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        VisualizerColors.gradientColor(settings.colorScheme, 0.2f),
                        VisualizerColors.gradientColor(settings.colorScheme, 0.6f),
                        VisualizerColors.gradientColor(settings.colorScheme, 0.95f),
                    ),
                    start = start,
                    end = end,
                ),
                start = start,
                end = end,
                strokeWidth = barWidth.coerceAtLeast(settings.thickness),
                cap = StrokeCap.Round,
            )

            if (settings.peakHoldEnabled && peak > mag + 0.02f) {
                val capR = d.innerR + (d.maxR - d.innerR) * peak
                val capCenter = polarToOffset(d.cx, d.cy, capR, angle)
                val perpAngle = angle + PI.toFloat() / 2f
                val halfW = barWidth * 0.6f
                val capStart = polarToOffset(capCenter.x, capCenter.y, halfW, perpAngle)
                val capEnd = polarToOffset(capCenter.x, capCenter.y, halfW, perpAngle + PI.toFloat())
                drawLine(
                    color = VisualizerColors.gradientColor(settings.colorScheme, 0.95f, 0.95f),
                    start = capStart,
                    end = capEnd,
                    strokeWidth = settings.thickness * 0.8f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
