package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.PlaylistListAdapter
import com.trioscg.androidapp5.utils.PlaylistManager

class AllPlaylistsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_playlists)

        // Bind RecyclerView from layout
        val recyclerView: RecyclerView = findViewById(R.id.playlistsRecyclerView)

        // Load all saved playlist names
        val playlistNames = PlaylistManager.getAllPlaylistNames().toMutableList()

        // Set up adapter and item click: launch PlaylistActivity with selected name
        val adapter = PlaylistListAdapter(playlistNames) { selected ->
            val intent = Intent(this, PlaylistActivity::class.java).apply {
                putExtra("playlist_name", selected)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Enable swipe-to-delete using ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val removed = adapter.removeAt(position)
                PlaylistManager.clearPlaylist(removed)
                Toast.makeText(this@AllPlaylistsActivity, "Deleted \"$removed\"", Toast.LENGTH_SHORT).show()
            }
        })

        // Attach swipe gesture to RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView)
    } // onCreate()
} // AllPlaylistsActivity