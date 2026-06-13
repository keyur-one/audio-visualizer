package com.example.audiovisualizer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.audiovisualizer.R
import com.example.audiovisualizer.data.ColorScheme
import com.example.audiovisualizer.data.VisualizerSettings
import com.example.audiovisualizer.visualizer.VisualizerType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelContent(
    settings: VisualizerSettings,
    captureRateHz: Int,
    onUpdate: ((VisualizerSettings) -> VisualizerSettings) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings), color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            VisualizerTypeDropdown(
                selected = settings.visualizerType,
                onSelected = { type -> onUpdate { it.copy(visualizerType = type) } },
            )

            ColorSchemeDropdown(
                selected = settings.colorScheme,
                onSelected = { scheme -> onUpdate { it.copy(colorScheme = scheme) } },
            )

            SliderSetting(
                label = stringResource(R.string.update_frequency),
                value = settings.updateFrequency,
                valueRange = 0.25f..1f,
                display = if (captureRateHz > 0) {
                    "${captureRateHz} Hz"
                } else {
                    "%.0f%%".format(settings.updateFrequency * 100)
                },
                onValueChange = { v -> onUpdate { it.copy(updateFrequency = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.bar_count),
                value = settings.barCount.toFloat(),
                valueRange = 16f..128f,
                steps = 14,
                display = settings.barCount.toString(),
                onValueChange = { v -> onUpdate { it.copy(barCount = v.roundToInt()) } },
            )

            SliderSetting(
                label = stringResource(R.string.line_thickness),
                value = settings.thickness,
                valueRange = 1f..8f,
                display = "%.1f".format(settings.thickness),
                onValueChange = { v -> onUpdate { it.copy(thickness = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.inner_radius),
                value = settings.innerRadius,
                valueRange = 0.05f..0.6f,
                display = "%.0f%%".format(settings.innerRadius * 100),
                onValueChange = { v -> onUpdate { it.copy(innerRadius = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.outer_scale),
                value = settings.outerScale,
                valueRange = 0.4f..1f,
                display = "%.0f%%".format(settings.outerScale * 100),
                onValueChange = { v -> onUpdate { it.copy(outerScale = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.sensitivity),
                value = settings.sensitivity,
                valueRange = 0.5f..3f,
                display = "%.1f".format(settings.sensitivity),
                onValueChange = { v -> onUpdate { it.copy(sensitivity = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.smoothing),
                value = settings.smoothing,
                valueRange = 0f..0.9f,
                display = "%.0f%%".format(settings.smoothing * 100),
                onValueChange = { v -> onUpdate { it.copy(smoothing = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.rotation_speed),
                value = settings.rotationSpeed,
                valueRange = 0f..1f,
                display = "%.2f".format(settings.rotationSpeed),
                onValueChange = { v -> onUpdate { it.copy(rotationSpeed = v) } },
            )

            SliderSetting(
                label = stringResource(R.string.glow_intensity),
                value = settings.glowIntensity,
                valueRange = 0f..1f,
                display = "%.0f%%".format(settings.glowIntensity * 100),
                onValueChange = { v -> onUpdate { it.copy(glowIntensity = v) } },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.peak_hold), color = Color.White)
                Switch(
                    checked = settings.peakHoldEnabled,
                    onCheckedChange = { enabled -> onUpdate { it.copy(peakHoldEnabled = enabled) } },
                )
            }

            SliderSetting(
                label = stringResource(R.string.peak_fall_speed),
                value = settings.peakFallSpeed,
                valueRange = 0.005f..0.1f,
                display = "%.3f".format(settings.peakFallSpeed),
                onValueChange = { v -> onUpdate { it.copy(peakFallSpeed = v) } },
            )

            Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.reset_defaults))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisualizerTypeDropdown(
    selected: VisualizerType,
    onSelected: (VisualizerType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.visualizer_style)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            VisualizerType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSchemeDropdown(
    selected: ColorScheme,
    onSelected: (ColorScheme) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.color_scheme)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ColorScheme.entries.forEach { scheme ->
                DropdownMenuItem(
                    text = { Text(scheme.label) },
                    onClick = {
                        onSelected(scheme)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = Color.White)
            Text(display, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )
    }
}
