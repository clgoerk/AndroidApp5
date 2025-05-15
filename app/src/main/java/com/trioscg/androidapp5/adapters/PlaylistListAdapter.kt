package com.trioscg.androidapp5.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trioscg.androidapp5.R

class PlaylistListAdapter(
    private val playlistNames: MutableList<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PlaylistListAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.playlistName)
    } // PlaylistViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    } // onCreateViewHolder()

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val name = playlistNames[position]
        holder.playlistName.text = name
        holder.itemView.setOnClickListener {
            onItemClick(name)
        }
    } // onBindViewHolder()

    override fun getItemCount(): Int = playlistNames.size

    fun removeAt(position: Int): String {
        val removed = playlistNames.removeAt(position)
        notifyItemRemoved(position)
        return removed
    } // removeAt()
} // PlaylistListAdapter