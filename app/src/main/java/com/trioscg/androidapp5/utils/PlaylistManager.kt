package com.trioscg.androidapp5.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trioscg.androidapp5.models.Playlist
import com.trioscg.androidapp5.models.VideoItem

object PlaylistManager {

    private const val PREFS_NAME = "playlist_prefs"
    private const val KEY_PREFIX = "playlist_"
    private const val KEY_PLAYLIST_NAMES = "playlist_names"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    // Must be called once in Application or MainActivity
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    } // init()

    // Adds a video to an existing or new playlist
    fun addVideoToPlaylist(video: VideoItem, name: String) {
        val playlist = getPlaylist(name) ?: Playlist(name, mutableListOf())
        playlist.videos.add(video)
        savePlaylist(name, playlist.videos)
    } // addVideoToPlaylist()

    // Returns a full Playlist object by name
    fun getPlaylist(name: String): Playlist? {
        val json = prefs.getString(KEY_PREFIX + name, null) ?: return null
        val type = object : TypeToken<Playlist>() {}.type
        return gson.fromJson(json, type)
    } // getPlaylist()

    // Saves playlist data to SharedPreferences
    fun savePlaylist(name: String, videos: List<VideoItem>) {
        val playlist = Playlist(name, videos.toMutableList())
        val json = gson.toJson(playlist)
        prefs.edit {
            putString(KEY_PREFIX + name, json)
        }
        savePlaylistName(name)
    } // savePlaylist()

    // Tracks all playlist names
    private fun savePlaylistName(name: String) {
        val names = getAllPlaylistNames().toMutableSet()
        names.add(name)
        prefs.edit {
            putStringSet(KEY_PLAYLIST_NAMES, names)
        }
    } // savePlaylistName()

    // Returns all stored playlist names
    fun getAllPlaylistNames(): Set<String> {
        return prefs.getStringSet(KEY_PLAYLIST_NAMES, emptySet()) ?: emptySet()
    } // getAllPlaylistNames()

    // Removes a playlist and updates name tracking
    fun clearPlaylist(name: String) {
        prefs.edit {
            remove(KEY_PREFIX + name)
        }
        val names = getAllPlaylistNames().toMutableSet()
        names.remove(name)
        prefs.edit {
            putStringSet(KEY_PLAYLIST_NAMES, names)
        }
    } // clearPlaylist()
} // PlaylistManager