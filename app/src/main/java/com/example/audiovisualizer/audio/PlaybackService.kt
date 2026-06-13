package com.example.audiovisualizer.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.audiovisualizer.MainActivity
import com.example.audiovisualizer.R
import com.example.audiovisualizer.data.MediaItem

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    companion object {
        private const val CHANNEL_ID = "playback"
        private const val NOTIFICATION_ID = 1

        @Volatile
        var instance: PlaybackService? = null
            private set
    }

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var isInForeground = false
    private val mainHandler = Handler(Looper.getMainLooper())

    val audioAnalyzer = AudioAnalyzer()

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { exoPlayer ->
                exoPlayer.addAnalyticsListener(object : AnalyticsListener {
                    override fun onAudioSessionIdChanged(
                        eventTime: AnalyticsListener.EventTime,
                        audioSessionId: Int,
                    ) {
                        if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                            audioAnalyzer.attachToSession(audioSessionId)
                        }
                    }
                })
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            scheduleVisualizerAttach(exoPlayer)
                        }
                        updateForegroundState(exoPlayer)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) scheduleVisualizerAttach(exoPlayer)
                        updateForegroundState(exoPlayer)
                    }

                    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                        if (isInForeground) {
                            updateNotification()
                        }
                    }
                })
            }

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun scheduleVisualizerAttach(exoPlayer: ExoPlayer) {
        attachVisualizer(exoPlayer)
        mainHandler.postDelayed({ attachVisualizer(exoPlayer) }, 300)
    }

    private fun attachVisualizer(exoPlayer: ExoPlayer) {
        val sessionId = exoPlayer.audioSessionId
        if (sessionId != C.AUDIO_SESSION_ID_UNSET) {
            audioAnalyzer.attachToSession(sessionId)
        }
    }

    private fun updateForegroundState(exoPlayer: ExoPlayer) {
        val shouldBeForeground = exoPlayer.isPlaying ||
            exoPlayer.playbackState == Player.STATE_BUFFERING
        if (shouldBeForeground) {
            promoteToForeground()
        } else if (
            isInForeground &&
            exoPlayer.playbackState != Player.STATE_BUFFERING &&
            !exoPlayer.isPlaying
        ) {
            demoteFromForeground()
        }
    }

    private fun promoteToForeground() {
        if (isInForeground) {
            updateNotification()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        isInForeground = true
    }

    private fun demoteFromForeground() {
        if (!isInForeground) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)
        }
        isInForeground = false
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_playback),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.channel_playback_desc)
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val launchIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val title = player?.mediaMetadata?.title?.toString()
            ?: getString(R.string.app_name)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(getString(R.string.notification_playing))
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(launchIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        if (!isInForeground) return
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    fun playMedia(item: MediaItem) {
        player?.apply {
            setMediaItem(ExoMediaItem.fromUri(item.uri))
            prepare()
            play()
        }
        promoteToForeground()
        player?.let { scheduleVisualizerAttach(it) }
    }

    fun getPlayer(): ExoPlayer? = player

    fun getAnalyzer(): AudioAnalyzer = audioAnalyzer

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = player ?: return
        if (!p.playWhenReady || p.mediaItemCount == 0) {
            demoteFromForeground()
            stopSelf()
        }
    }

    override fun onDestroy() {
        instance = null
        audioAnalyzer.release()
        mediaSession?.run {
            player?.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
