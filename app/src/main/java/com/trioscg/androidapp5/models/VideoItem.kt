package com.trioscg.androidapp5.models

data class VideoResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val id: String,
    val snippet: Snippet,
    val statistics: Statistics,
    val contentDetails: ContentDetails,
    val status: Status,
    val player: Player,
    val topicDetails: TopicDetails?,
    val recordingDetails: RecordingDetails?
)

data class Snippet(
    val title: String,
    val description: String,
    val channelTitle: String,
    val publishedAt: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val medium: Thumbnail
)

data class Thumbnail(
    val url: String
)

data class Statistics(
    val viewCount: String,
    val likeCount: String?,
    val commentCount: String?
)

data class ContentDetails(
    val duration: String,
    val definition: String
)

data class Status(
    val privacyStatus: String
)

data class Player(
    val embedHtml: String
)

data class TopicDetails(
    val topicCategories: List<String>?
)

data class RecordingDetails(
    val recordingDate: String?
)