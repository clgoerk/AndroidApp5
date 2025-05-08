package com.trioscg.androidapp5.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.models.VideoItem

class VideoAdapter(
    private val videos: List<VideoItem>,
    private val onItemClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnailImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val channelText: TextView = itemView.findViewById(R.id.channelText)
        val viewsText: TextView = itemView.findViewById(R.id.viewsText)
    } // VideoViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    } // onCreateViewHolder()

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.titleText.text = video.snippet.title
        holder.channelText.text = video.snippet.channelTitle
        holder.viewsText.text = buildString {
            append(video.statistics.viewCount)
            append(" views")
        }

        Glide.with(holder.itemView.context)
            .load(video.snippet.thumbnails.medium.url)
            .into(holder.thumbnailImage)

        holder.itemView.setOnClickListener {
            onItemClick(video)
        }
    } // onBindViewHolder()

    override fun getItemCount(): Int = videos.size
} // VideoAdapter