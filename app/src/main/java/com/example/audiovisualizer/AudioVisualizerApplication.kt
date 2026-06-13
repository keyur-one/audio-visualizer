package com.example.audiovisualizer

import android.app.Application
import com.example.audiovisualizer.data.SettingsRepository

class AudioVisualizerApplication : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
    }
}
