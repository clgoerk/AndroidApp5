package com.trioscg.androidapp5.models

data class Playlist(
    val name: String,
    val videos: MutableList<VideoItem>
)