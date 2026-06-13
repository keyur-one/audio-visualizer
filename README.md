# Audio Visualizer

Android app that plays local audio and video files with real-time **circular audio visualizations**. Built with Kotlin, Jetpack Compose, ExoPlayer (Media3), and the Android `Visualizer` API.

## Features

- **10 circular visualizer styles** — concentric bars, ribbons, radial lines, particle paths, spectrum peaks, and more
- **Local media library** — scan and play audio/video from device storage
- **Background playback** — keeps playing with a media notification when the app is minimized
- **Customizable visuals** — bar count, colors, sensitivity, smoothing, rotation, glow, peak hold, and update frequency
- **Single-screen UI** — visualizer with slide-in library (left) and settings (right)

## Visualizer styles

| Style | Description |
|-------|-------------|
| Concentric Bar Ring | Layered rectangular bars in a ring |
| Oscillating Ribbon | Overlapping wavy frequency ribbons |
| Radial Dotted Bars | Square dots radiating outward |
| Wavy Particle Path | Dotted circular wave path |
| Classic Radial Lines | Thin lines with varying length |
| Fluid Wave Ribbon | Smooth, liquid ribbon layers |
| Swirling Hairy Circle | Short curved “hair” lines |
| Parallel Line Sphere | Parallel curved lines (sphere look) |
| Dense Radial Spikes | Tight radial spike pattern |
| Circular Spectrum Peaks | Radial bars with peak-hold caps |

## Requirements

- Android **13+** (API 33)
- Storage permission for media scanning
- Microphone permission (required by the Visualizer API on many devices)

## Build & run

1. Clone the repository:
   ```bash
   git clone https://github.com/keyur-one/audio-visualizer.git
   cd audio-visualizer
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run on a device or emulator.

Or from the command line:

```bash
./gradlew assembleDebug
```

## Usage

1. Grant permissions when prompted (storage, notifications, audio).
2. Tap **☰** (top-left) to open the library and select a track.
3. Tap **⋮** (top-right) to adjust visualizer settings.
4. Use the bottom controls to play, pause, and seek.

## Tech stack

- **UI:** Jetpack Compose, Material 3
- **Playback:** Media3 ExoPlayer, foreground `PlaybackService`
- **Audio analysis:** Android `Visualizer` (FFT + waveform)
- **Settings:** DataStore Preferences

## Update frequency

The **Update Frequency** setting controls the Visualizer capture rate as a fraction of the device maximum (`Visualizer.getMaxCaptureRate()`). On many devices the cap is about **20 Hz**. On-screen animation still runs at display refresh rate.

## Project structure

```
app/src/main/java/com/example/audiovisualizer/
├── audio/          # Playback service, ExoPlayer, FFT analyzer
├── data/           # Media library, settings (DataStore)
├── ui/             # Compose screens and overlay panels
└── visualizer/     # Circular visualizer renderers
```

## License

This project is provided as-is for personal and educational use.
