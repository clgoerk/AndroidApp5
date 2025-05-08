package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.VideoAdapter
import com.trioscg.androidapp5.api.RetrofitClient
import com.trioscg.androidapp5.api.YouTubeApiService
import com.trioscg.androidapp5.models.SearchResponse
import com.trioscg.androidapp5.models.VideoItem
import com.trioscg.androidapp5.models.VideoResponse
import com.trioscg.androidapp5.utils.PlaylistManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val apiKey = "AIzaSyD01YGdmpOADhEGsXYZbH_07g15tqXxDuY"
    private val playlistName = "playlist" // ✅ Consistent name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        recyclerView = findViewById(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val query = intent.getStringExtra("query")
        if (query.isNullOrEmpty()) {
            Toast.makeText(this, "No search term provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        searchVideos(query)
    } // onCreate()

    private fun searchVideos(query: String) {
        val service = RetrofitClient.instance.create(YouTubeApiService::class.java)

        service.searchVideos(q = query, apiKey = apiKey)
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    if (response.isSuccessful) {
                        val searchItems = response.body()?.items ?: emptyList()
                        val videoIds = searchItems.joinToString(",") { it.id.videoId }

                        service.getVideoDetails(id = videoIds, apiKey = apiKey)
                            .enqueue(object : Callback<VideoResponse> {
                                override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                                    if (response.isSuccessful) {
                                        val fullVideos = response.body()?.items ?: emptyList()
                                        recyclerView.adapter = VideoAdapter(fullVideos) { video ->
                                            showVideoOptionsDialog(video)
                                        }
                                    } else {
                                        showError("Failed to load video details: ${response.code()}")
                                    }
                                }

                                override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                                    showError("Error fetching video details: ${t.message}")
                                }
                            })
                    } else {
                        showError("Search failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    showError("Search error: ${t.message}")
                }
            })
    } // searchVideos()

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
    } // showError()
} // SearchResultsActivity