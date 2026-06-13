package com.example.audiovisualizer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.audiovisualizer.visualizer.VisualizerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "visualizer_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val VISUALIZER_TYPE = stringPreferencesKey("visualizer_type")
        val BAR_COUNT = intPreferencesKey("bar_count")
        val THICKNESS = floatPreferencesKey("thickness")
        val INNER_RADIUS = floatPreferencesKey("inner_radius")
        val OUTER_SCALE = floatPreferencesKey("outer_scale")
        val SENSITIVITY = floatPreferencesKey("sensitivity")
        val SMOOTHING = floatPreferencesKey("smoothing")
        val ROTATION_SPEED = floatPreferencesKey("rotation_speed")
        val GLOW_INTENSITY = floatPreferencesKey("glow_intensity")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val PEAK_HOLD = booleanPreferencesKey("peak_hold")
        val PEAK_FALL_SPEED = floatPreferencesKey("peak_fall_speed")
        val UPDATE_FREQUENCY = floatPreferencesKey("update_frequency")
    }

    val settings: Flow<VisualizerSettings> = context.dataStore.data.map { prefs ->
        VisualizerSettings(
            visualizerType = prefs[Keys.VISUALIZER_TYPE]?.let { name ->
                VisualizerType.entries.find { it.name == name }
            } ?: VisualizerSettings.Default.visualizerType,
            barCount = prefs[Keys.BAR_COUNT] ?: VisualizerSettings.Default.barCount,
            thickness = prefs[Keys.THICKNESS] ?: VisualizerSettings.Default.thickness,
            innerRadius = prefs[Keys.INNER_RADIUS] ?: VisualizerSettings.Default.innerRadius,
            outerScale = prefs[Keys.OUTER_SCALE] ?: VisualizerSettings.Default.outerScale,
            sensitivity = prefs[Keys.SENSITIVITY] ?: VisualizerSettings.Default.sensitivity,
            smoothing = prefs[Keys.SMOOTHING] ?: VisualizerSettings.Default.smoothing,
            rotationSpeed = prefs[Keys.ROTATION_SPEED] ?: VisualizerSettings.Default.rotationSpeed,
            glowIntensity = prefs[Keys.GLOW_INTENSITY] ?: VisualizerSettings.Default.glowIntensity,
            colorScheme = prefs[Keys.COLOR_SCHEME]?.let { name ->
                ColorScheme.entries.find { it.name == name }
            } ?: VisualizerSettings.Default.colorScheme,
            peakHoldEnabled = prefs[Keys.PEAK_HOLD] ?: VisualizerSettings.Default.peakHoldEnabled,
            peakFallSpeed = prefs[Keys.PEAK_FALL_SPEED] ?: VisualizerSettings.Default.peakFallSpeed,
            updateFrequency = prefs[Keys.UPDATE_FREQUENCY] ?: VisualizerSettings.Default.updateFrequency,
        )
    }

    suspend fun updateSettings(transform: (VisualizerSettings) -> VisualizerSettings) {
        context.dataStore.edit { prefs ->
            val current = VisualizerSettings(
                visualizerType = prefs[Keys.VISUALIZER_TYPE]?.let { name ->
                    VisualizerType.entries.find { it.name == name }
                } ?: VisualizerSettings.Default.visualizerType,
                barCount = prefs[Keys.BAR_COUNT] ?: VisualizerSettings.Default.barCount,
                thickness = prefs[Keys.THICKNESS] ?: VisualizerSettings.Default.thickness,
                innerRadius = prefs[Keys.INNER_RADIUS] ?: VisualizerSettings.Default.innerRadius,
                outerScale = prefs[Keys.OUTER_SCALE] ?: VisualizerSettings.Default.outerScale,
                sensitivity = prefs[Keys.SENSITIVITY] ?: VisualizerSettings.Default.sensitivity,
                smoothing = prefs[Keys.SMOOTHING] ?: VisualizerSettings.Default.smoothing,
                rotationSpeed = prefs[Keys.ROTATION_SPEED] ?: VisualizerSettings.Default.rotationSpeed,
                glowIntensity = prefs[Keys.GLOW_INTENSITY] ?: VisualizerSettings.Default.glowIntensity,
                colorScheme = prefs[Keys.COLOR_SCHEME]?.let { name ->
                    ColorScheme.entries.find { it.name == name }
                } ?: VisualizerSettings.Default.colorScheme,
                peakHoldEnabled = prefs[Keys.PEAK_HOLD] ?: VisualizerSettings.Default.peakHoldEnabled,
                peakFallSpeed = prefs[Keys.PEAK_FALL_SPEED] ?: VisualizerSettings.Default.peakFallSpeed,
                updateFrequency = prefs[Keys.UPDATE_FREQUENCY] ?: VisualizerSettings.Default.updateFrequency,
            )
            val updated = transform(current)
            prefs[Keys.VISUALIZER_TYPE] = updated.visualizerType.name
            prefs[Keys.BAR_COUNT] = updated.barCount
            prefs[Keys.THICKNESS] = updated.thickness
            prefs[Keys.INNER_RADIUS] = updated.innerRadius
            prefs[Keys.OUTER_SCALE] = updated.outerScale
            prefs[Keys.SENSITIVITY] = updated.sensitivity
            prefs[Keys.SMOOTHING] = updated.smoothing
            prefs[Keys.ROTATION_SPEED] = updated.rotationSpeed
            prefs[Keys.GLOW_INTENSITY] = updated.glowIntensity
            prefs[Keys.COLOR_SCHEME] = updated.colorScheme.name
            prefs[Keys.PEAK_HOLD] = updated.peakHoldEnabled
            prefs[Keys.PEAK_FALL_SPEED] = updated.peakFallSpeed
            prefs[Keys.UPDATE_FREQUENCY] = updated.updateFrequency
        }
    }

    suspend fun resetToDefaults() {
        updateSettings { VisualizerSettings.Default }
    }
}
