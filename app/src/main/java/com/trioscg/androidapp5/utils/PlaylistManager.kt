package com.trioscg.androidapp5.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trioscg.androidapp5.models.Playlist
import com.trioscg.androidapp5.models.VideoItem
import androidx.core.content.edit

object PlaylistManager {

    private const val PREFS_NAME = "playlist_prefs"
    private const val KEY_PREFIX = "playlist_"
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    } // init()
    fun addVideoToPlaylist(video: VideoItem, name: String) {
        val playlist = getPlaylist(name) ?: Playlist(name, mutableListOf())
        playlist.videos.add(video)
        savePlaylist(playlist)
    } // addVideoToPlaylist()

    fun getPlaylist(name: String): Playlist? {
        val json = prefs.getString(KEY_PREFIX + name, null) ?: return null
        val type = object : TypeToken<Playlist>() {}.type
        return gson.fromJson(json, type)
    } // getPlaylist()

    private fun savePlaylist(playlist: Playlist) {
        val json = gson.toJson(playlist)
        prefs.edit() { putString(KEY_PREFIX + playlist.name, json) }
    } // savePlaylist()

    fun clearPlaylist(name: String) {
        prefs.edit() { remove(KEY_PREFIX + name) }
    }
} // PlaylistManager