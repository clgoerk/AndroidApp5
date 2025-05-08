package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.VideoAdapter
import com.trioscg.androidapp5.api.RetrofitClient
import com.trioscg.androidapp5.api.YouTubeApiService
import com.trioscg.androidapp5.models.VideoItem
import com.trioscg.androidapp5.models.VideoResponse
import com.trioscg.androidapp5.utils.PlaylistManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private val apiKey = "AIzaSyD01YGdmpOADhEGsXYZbH_07g15tqXxDuY"
    private val playlistName = "playlist" // ✅ Consistent playlist name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PlaylistManager.init(applicationContext)

        videoRecyclerView = findViewById(R.id.videoRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)

        videoRecyclerView.layoutManager = LinearLayoutManager(this)

        val viewPlaylistButton = findViewById<Button>(R.id.viewPlaylistButton)
        viewPlaylistButton.setOnClickListener {
            val intent = Intent(this, PlaylistActivity::class.java)
            startActivity(intent)
        }

        fetchTrendingVideos()

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(this, SearchResultsActivity::class.java)
                    intent.putExtra("query", query)
                    startActivity(intent)
                }
                true
            } else false
        }

        searchButton.setOnClickListener {
            submitSearch()
        }
    } // onCreate()

    private fun submitSearch() {
        val query = searchInput.text.toString().trim()
        if (query.isNotEmpty()) {
            val intent = Intent(this, SearchResultsActivity::class.java)
            intent.putExtra("query", query)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
        }
    } // submitSearch()

    private fun fetchTrendingVideos() {
        val service = RetrofitClient.instance.create(YouTubeApiService::class.java)

        service.getTrendingMusicVideos(apiKey = apiKey)
            .enqueue(object : Callback<VideoResponse> {
                override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                    if (response.isSuccessful) {
                        val videoList = response.body()?.items ?: emptyList()
                        videoRecyclerView.adapter = VideoAdapter(videoList) { video ->
                            showVideoOptionsDialog(video)
                        }
                    } else {
                        showError("Failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                    showError("Error: ${t.message}")
                }
            })
    } // fetchTrendingVideos()

    private fun showVideoOptionsDialog(video: VideoItem) {
        val options = arrayOf("Play Video", "Add to Playlist")

        AlertDialog.Builder(this)
            .setTitle(video.snippet.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, VideoDetailActivity::class.java).apply {
                            putExtra("video_id", video.id)
                            putExtra("title", video.snippet.title)
                            putExtra("description", video.snippet.description)
                            putExtra("channel", video.snippet.channelTitle)
                            putExtra("views", video.statistics.viewCount)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        PlaylistManager.addVideoToPlaylist(video, playlistName)
                        Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    } // showVideoOptionsDialog()

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
} // showError()