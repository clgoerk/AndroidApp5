package com.trioscg.androidapp5.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.models.VideoItem
import com.trioscg.androidapp5.utils.PlaylistManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: YouTubePlayerView
    private lateinit var titleView: TextView
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var youTubePlayer: YouTubePlayer

    private val handler = Handler(Looper.getMainLooper())

    private var currentIndex = 0
    private var currentPlaylist: List<VideoItem> = emptyList()
    private var videoDuration = 0f
    private var currentSecond = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable full immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_player)

        // View bindings
        playerView = findViewById(R.id.youtubePlayerView)
        titleView = findViewById(R.id.titleView)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)

        lifecycle.addObserver(playerView)

        // Restore state if available
        currentIndex = savedInstanceState?.getInt("current_index") ?: 0
        currentSecond = savedInstanceState?.getFloat("current_second") ?: 0f

        // Load playlist
        val playlistName = intent.getStringExtra("playlist_name") ?: "playlist"
        currentPlaylist = PlaylistManager.getPlaylist(playlistName)?.videos ?: emptyList()
        if (currentPlaylist.isEmpty()) {
            finish()
            return
        }

        // Init media session for metadata broadcast (e.g., Bluetooth, car display)
        mediaSession = MediaSessionCompat(this, "PlaylistPlayerSession").apply {
            isActive = true
        }

        // Setup YouTube player listeners
        playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                playVideoAtIndex(currentIndex, currentSecond)
            }

            override fun onVideoDuration(player: YouTubePlayer, duration: Float) {
                videoDuration = duration
            }

            override fun onCurrentSecond(player: YouTubePlayer, second: Float) {
                currentSecond = second
            }
        })

        // Next/Previous controls
        nextButton.setOnClickListener {
            if (currentIndex < currentPlaylist.size - 1) {
                currentIndex++
                currentSecond = 0f
                playVideoAtIndex(currentIndex)
            }
        }

        prevButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                currentSecond = 0f
                playVideoAtIndex(currentIndex)
            }
        }

        // Tap to show title overlay
        playerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                toggleTitleVisibilityTemporarily()
            }
            false
        }
    } // onCreate()

    private fun playVideoAtIndex(index: Int, startSeconds: Float = 0f) {
        val video = currentPlaylist[index]
        youTubePlayer.loadVideo(video.id, startSeconds)

        titleView.text = video.snippet.title
        titleView.visibility = TextView.VISIBLE

        // Load artwork and update metadata
        val artworkUrl = "https://img.youtube.com/vi/${video.id}/hqdefault.jpg"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap: Bitmap = Glide.with(this@PlaylistPlayerActivity)
                    .asBitmap()
                    .load(artworkUrl)
                    .submit()
                    .get()

                mediaSession.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, video.snippet.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, video.snippet.channelTitle)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                        .build()
                )
            } catch (_: Exception) {
                mediaSession.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, video.snippet.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, video.snippet.channelTitle)
                        .build()
                )
            }
        }

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ titleView.visibility = TextView.GONE }, 2000)
    } // playVideoAtIndex()

    private fun toggleTitleVisibilityTemporarily() {
        titleView.visibility = TextView.VISIBLE
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ titleView.visibility = TextView.GONE }, 2000)
    } // toggleTitleVisibilityTemporarily()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_index", currentIndex)
        outState.putFloat("current_second", currentSecond)
    } // onSaveInstanceState()

    override fun onDestroy() {
        playerView.release()
        mediaSession.release()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    } // onDestroy()
} // PlaylistPlayerActivity