package com.trioscg.androidapp5.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.bumptech.glide.Glide
import com.trioscg.androidapp5.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresPermission

class MusicService : Service() {

    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "Unknown Title"
        val artist = intent?.getStringExtra("artist") ?: "Unknown Artist"
        val videoId = intent?.getStringExtra("video_id")
        val artworkUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        updateMetadata(title, artist, artworkUrl)
        return START_NOT_STICKY
    }

    private fun updateMetadata(title: String, artist: String, artworkUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap: Bitmap = Glide.with(this@MusicService)
                    .asBitmap()
                    .load(artworkUrl)
                    .submit()
                    .get()

                val metadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    .build()

                mediaSession.setMetadata(metadata)

                @Suppress("MissingPermission")
                showNotification(title, artist, bitmap)

            } catch (_: Exception) {
                val fallback = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .build()

                mediaSession.setMetadata(fallback)

                @Suppress("MissingPermission")
                showNotification(title, artist, null)
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, artist: String, artwork: Bitmap?) {
        val builder = NotificationCompat.Builder(this, "media_channel")
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setStyle(MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (artwork != null) {
            builder.setLargeIcon(artwork)
        }

        NotificationManagerCompat.from(this).notify(1, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? = null
}