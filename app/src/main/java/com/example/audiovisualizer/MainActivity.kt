package com.example.audiovisualizer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audiovisualizer.audio.PlaybackViewModel
import com.example.audiovisualizer.data.MediaRepository
import com.example.audiovisualizer.ui.AudioVisualizerApp
import com.example.audiovisualizer.ui.theme.AudioVisualizerTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        val app = application as AudioVisualizerApplication

        setContent {
            AudioVisualizerTheme(darkTheme = true, dynamicColor = false) {
                val viewModel: PlaybackViewModel = viewModel(
                    factory = PlaybackViewModel.Factory(
                        MediaRepository(this),
                        app.settingsRepository,
                    ),
                )

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    viewModel.startService(this@MainActivity)
                }

                AudioVisualizerApp(viewModel = viewModel)
            }
        }
    }
}
