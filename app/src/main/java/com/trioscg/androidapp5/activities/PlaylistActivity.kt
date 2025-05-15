package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.VideoAdapter
import com.trioscg.androidapp5.utils.PlaylistManager

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var playAllButton: Button
    private var selectedPlaylistName: String = "playlist"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        playlistRecyclerView = findViewById(R.id.playlistRecyclerView)
        playAllButton = findViewById(R.id.playAllButton)

        playlistRecyclerView.layoutManager = LinearLayoutManager(this)

        val name = intent.getStringExtra("playlist_name")
        if (name != null) {
            selectedPlaylistName = name
            loadPlaylist(name)
        } else {
            finish()
        }
    } // onCreate()

    private fun showPlaylistSelectionDialog() {
        val playlistNames = PlaylistManager.getAllPlaylistNames().toList()

        if (playlistNames.isEmpty()) {
            Toast.makeText(this, "No playlists found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Select a Playlist")
            .setItems(playlistNames.toTypedArray()) { _, which ->
                selectedPlaylistName = playlistNames[which]
                loadPlaylist(selectedPlaylistName)
            }
            .setCancelable(false)
            .show()
    } // showPlaylistSelectionDialog()

    private fun loadPlaylist(playlistName: String) {
        // Set the playlist title in the TextView
        findViewById<TextView>(R.id.playlistTitle).text = playlistName

        val playlist = PlaylistManager.getPlaylist(playlistName)

        if (playlist == null || playlist.videos.isEmpty()) {
            Toast.makeText(this, "Playlist \"$playlistName\" is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val videoList = playlist.videos.toMutableList()

        val adapter = VideoAdapter(videoList) { video ->
            val intent = Intent(this, VideoDetailActivity::class.java).apply {
                putExtra("video_id", video.id)
                putExtra("title", video.snippet.title)
                putExtra("description", video.snippet.description)
                putExtra("channel", video.snippet.channelTitle)
                putExtra("views", video.statistics.viewCount)
            }
            startActivity(intent)
        }

        playlistRecyclerView.adapter = adapter

        // Swipe-to-delete support
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val removed = videoList.removeAt(position)
                PlaylistManager.savePlaylist(playlistName, videoList)
                adapter.notifyItemRemoved(position)
                Toast.makeText(
                    this@PlaylistActivity,
                    "Removed: ${removed.snippet.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        itemTouchHelper.attachToRecyclerView(playlistRecyclerView)

        playAllButton.setOnClickListener {
            val intent = Intent(this, PlaylistPlayerActivity::class.java)
            intent.putExtra("playlist_name", playlistName)
            startActivity(intent)
        }
    }
} // PlaylistActivity