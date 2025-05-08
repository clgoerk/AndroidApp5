package com.trioscg.androidapp5.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.models.VideoItem
import com.trioscg.androidapp5.utils.PlaylistManager

class PlaylistPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: YouTubePlayerView
    private lateinit var titleView: TextView
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton

    private var currentIndex = 0
    private var currentPlaylist: List<VideoItem> = emptyList()
    private var videoDuration = 0f
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var youTubePlayer: YouTubePlayer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_player)

        playerView = findViewById(R.id.youtubePlayerView)
        titleView = findViewById(R.id.titleView)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)

        lifecycle.addObserver(playerView)

        val playlistName = intent.getStringExtra("playlist_name") ?: "playlist"
        currentPlaylist = PlaylistManager.getPlaylist(playlistName)?.videos ?: emptyList()

        if (currentPlaylist.isEmpty()) {
            finish()
            return
        }

        playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                playVideoAtIndex(currentIndex)

                playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    private var lastSecond = 0f

                    override fun onVideoDuration(ytPlayer: YouTubePlayer, duration: Float) {
                        videoDuration = duration
                    }

                    override fun onCurrentSecond(ytPlayer: YouTubePlayer, second: Float) {
                        lastSecond = second
                        if (videoDuration > 0 && second >= videoDuration - 0.5f) {
                            currentIndex++
                            if (currentIndex < currentPlaylist.size) {
                                playVideoAtIndex(currentIndex)
                            }
                        }
                    }
                })

                nextButton.setOnClickListener {
                    if (currentIndex < currentPlaylist.size - 1) {
                        currentIndex++
                        playVideoAtIndex(currentIndex)
                    }
                }

                prevButton.setOnClickListener {
                    if (currentIndex > 0) {
                        currentIndex--
                        playVideoAtIndex(currentIndex)
                    }
                }
            }
        })

        playerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                toggleTitleVisibilityTemporarily()
            }
            false
        }
    } // oncCreate()

    private fun playVideoAtIndex(index: Int) {
        val video = currentPlaylist[index]
        youTubePlayer.loadVideo(video.id, 0f)
        titleView.text = video.snippet.title
        titleView.visibility = TextView.VISIBLE
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ titleView.visibility = TextView.GONE }, 2000)
    } // playVideoAtIndex()

    private fun toggleTitleVisibilityTemporarily() {
        titleView.visibility = TextView.VISIBLE
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ titleView.visibility = TextView.GONE }, 2000)
    } // toggleTitleVisibilityTemporarily()

    override fun onDestroy() {
        playerView.release()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    } // onDestroy()
} // PlaylistPlayerActivity