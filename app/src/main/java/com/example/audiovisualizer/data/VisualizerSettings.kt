package com.example.audiovisualizer.data

import com.example.audiovisualizer.visualizer.VisualizerType

enum class ColorScheme(val label: String) {
    NeonMagenta("Neon Magenta"),
    OceanCyan("Ocean Cyan"),
    Sunset("Sunset"),
    LimeBlue("Lime Blue"),
    PurpleHaze("Purple Haze"),
    Spectrum("Full Spectrum"),
}

data class VisualizerSettings(
    val visualizerType: VisualizerType = VisualizerType.ConcentricBarRing,
    val barCount: Int = 64,
    val thickness: Float = 3f,
    val innerRadius: Float = 0.25f,
    val outerScale: Float = 0.85f,
    val sensitivity: Float = 1.5f,
    val smoothing: Float = 0.65f,
    val rotationSpeed: Float = 0.15f,
    val glowIntensity: Float = 0.6f,
    val colorScheme: ColorScheme = ColorScheme.NeonMagenta,
    val peakHoldEnabled: Boolean = true,
    val peakFallSpeed: Float = 0.03f,
    /** Fraction of max Visualizer capture rate (0.25–1.0). Higher = smoother, more responsive. */
    val updateFrequency: Float = 0.75f,
) {
    companion object {
        val Default = VisualizerSettings()
    }
}
