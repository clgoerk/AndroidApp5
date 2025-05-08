package com.trioscg.androidapp5.api

import com.trioscg.androidapp5.models.SearchResponse
import com.trioscg.androidapp5.models.VideoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    // Trending
    @GET("videos")
    fun getTrendingMusicVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics,status,player,topicDetails,recordingDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("regionCode") regionCode: String = "US",
        @Query("videoCategoryId") videoCategoryId: String = "10", // Music
        @Query("maxResults") maxResults: Int = 50,
        @Query("key") apiKey: String
    ): Call<VideoResponse>

    // Search results
    @GET("search")
    fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 50,
        @Query("q") q: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10", // Music
        @Query("key") apiKey: String
    ): Call<SearchResponse>

    //  Fetch full details
    @GET("videos")
    fun getVideoDetails(
        @Query("part") part: String = "snippet,contentDetails,statistics,status,player,topicDetails,recordingDetails",
        @Query("id") id: String,
        @Query("key") apiKey: String
    ): Call<VideoResponse>
} // YouTubeApiService