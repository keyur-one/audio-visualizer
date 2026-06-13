package com.example.audiovisualizer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE040FB),
    secondary = Color(0xFF7B2CBF),
    tertiary = Color(0xFF00B4D8),
    background = Color(0xFF0D0221),
    surface = Color(0xFF1A0A2E),
)

@Composable
fun AudioVisualizerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}