package com.trioscg.androidapp5.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.trioscg.androidapp5.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoDetailActivity : AppCompatActivity() {

    private lateinit var playerView: YouTubePlayerView
    private lateinit var youTubePlayer: YouTubePlayer
    private lateinit var mediaSession: MediaSessionCompat

    private var currentSecond = 0f
    private lateinit var videoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)

        // Restore or retrieve videoId from intent
        videoId = savedInstanceState?.getString("video_id")
            ?: intent.getStringExtra("video_id") ?: return

        // Extract video metadata
        val title = intent.getStringExtra("title") ?: "Unknown Title"
        val description = intent.getStringExtra("description")
        val channel = intent.getStringExtra("channel") ?: "Unknown Channel"
        val views = intent.getStringExtra("views") ?: "0"

        // Restore playback position if available
        currentSecond = savedInstanceState?.getFloat("current_second") ?: 0f

        // MediaSession is used to send metadata (title/artist/artwork) to Bluetooth, USB, etc.
        mediaSession = MediaSessionCompat(this, "VideoDetailSession")
        mediaSession.isActive = false

        // Setup YouTube player
        playerView = findViewById(R.id.youtubePlayerView)
        lifecycle.addObserver(playerView)

        playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                player.loadVideo(videoId, currentSecond)

                // Load video thumbnail and attach metadata
                val artworkUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val bitmap: Bitmap = Glide.with(this@VideoDetailActivity)
                            .asBitmap()
                            .load(artworkUrl)
                            .submit()
                            .get()

                        // Set metadata with artwork
                        mediaSession.setMetadata(
                            MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channel)
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                                .build()
                        )
                    } catch (_: Exception) {
                        // Set metadata without artwork fallback
                        mediaSession.setMetadata(
                            MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channel)
                                .build()
                        )
                    }

                    // Activate session after metadata is ready
                    mediaSession.isActive = true
                }
            }

            // Keep track of current playback position to restore after rotation
            override fun onCurrentSecond(player: YouTubePlayer, second: Float) {
                currentSecond = second
            }
        })

        // Populate text views with video data
        findViewById<TextView>(R.id.titleTextView).text = title
        findViewById<TextView>(R.id.channelTextView).text = channel
        findViewById<TextView>(R.id.viewsTextView).text = buildString {
            append(views)
            append(" views")
        }
        findViewById<TextView>(R.id.descriptionTextView).text =
            if (!description.isNullOrBlank()) description else "No description available."
    } // onCreate()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat("current_second", currentSecond) // Save playback time
        outState.putString("video_id", videoId)            // Save video ID
    } // onSaveInstanceState()

    override fun onDestroy() {
        playerView.release()       // Release YouTube player resources
        mediaSession.release()     // Release media session to clean up metadata broadcast
        super.onDestroy()
    } // onDestroy()
} // VideoDetailActivity