package com.trioscg.androidapp5.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.trioscg.androidapp5.R

class VideoDetailActivity : AppCompatActivity() {

    private lateinit var playerView: YouTubePlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)

        val videoId = intent.getStringExtra("video_id") ?: return
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val channel = intent.getStringExtra("channel")
        val views = intent.getStringExtra("views")

        playerView = findViewById(R.id.youtubePlayerView)
        lifecycle.addObserver(playerView)

        playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

        findViewById<TextView>(R.id.titleTextView).text = title
        findViewById<TextView>(R.id.channelTextView).text = channel
        findViewById<TextView>(R.id.viewsTextView).text = buildString {
            append(views)
            append(" views")
        }
        findViewById<TextView>(R.id.descriptionTextView).text =
            if (!description.isNullOrBlank()) description else "No description available."
    }

    override fun onDestroy() {
        playerView.release()
        super.onDestroy()
    }
}