package com.trioscg.androidapp5.models


data class SearchResponse(
    val items: List<SearchVideoItem>
)

data class SearchVideoItem(
    val id: VideoId,
    val snippet: Snippet
)

data class VideoId(
    val videoId: String
)