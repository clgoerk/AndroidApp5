package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.VideoAdapter
import com.trioscg.androidapp5.utils.PlaylistManager

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var playAllButton: Button
    private val playlistName = "playlist"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        playlistRecyclerView = findViewById(R.id.playlistRecyclerView)
        playAllButton = findViewById(R.id.playAllButton)

        playlistRecyclerView.layoutManager = LinearLayoutManager(this)

        val playlist = PlaylistManager.getPlaylist(playlistName)

        if (playlist == null || playlist.videos.isEmpty()) {
            Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show()
            return
        }

        playlistRecyclerView.adapter = VideoAdapter(playlist.videos) { video ->
            val intent = Intent(this, VideoDetailActivity::class.java).apply {
                putExtra("video_id", video.id)
                putExtra("title", video.snippet.title)
                putExtra("description", video.snippet.description)
                putExtra("channel", video.snippet.channelTitle)
                putExtra("views", video.statistics.viewCount)
            }
            startActivity(intent)
        }

        playAllButton.setOnClickListener {
            val intent = Intent(this, PlaylistPlayerActivity::class.java)
            intent.putExtra("playlist_name", playlistName)
            startActivity(intent)
        }
    } // onCreate()
} // PlaylistActivity