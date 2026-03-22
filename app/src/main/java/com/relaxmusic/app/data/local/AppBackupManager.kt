package com.relaxmusic.app.data.local

import android.content.Context
import com.relaxmusic.app.data.db.dao.PlaylistDao
import com.relaxmusic.app.data.db.dao.SettingsDao
import com.relaxmusic.app.data.db.entity.PlaylistEntity
import com.relaxmusic.app.data.db.entity.PlaylistSongEntity
import com.relaxmusic.app.data.db.entity.SettingsEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppBackupManager(
    private val context: Context,
    private val settingsDao: SettingsDao,
    private val playlistDao: PlaylistDao
) {
    suspend fun exportBackup(): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val root = context.getExternalFilesDir("backup") ?: context.filesDir
            if (!root.exists()) root.mkdirs()
            val file = File(root, "relaxmusic_backup.json")

            val settingsKeys = listOf("library_tree_uri", "library_tree_uris")
            val settingsArray = JSONArray()
            settingsKeys.forEach { key ->
                settingsDao.get(key)?.let { entity ->
                    settingsArray.put(JSONObject().put("key", entity.key).put("value", entity.value))
                }
            }

            val playlistsArray = JSONArray()
            val playlistLinksArray = JSONArray()
            playlistDao.observePlaylistsSnapshot().forEach { playlist ->
                playlistsArray.put(JSONObject().put("id", playlist.id).put("name", playlist.name))
                playlistDao.getPlaylistSongIds(playlist.id).forEach { songId ->
                    playlistLinksArray.put(JSONObject().put("playlistId", playlist.id).put("songId", songId))
                }
            }

            val rootJson = JSONObject()
                .put("settings", settingsArray)
                .put("playlists", playlistsArray)
                .put("playlistSongs", playlistLinksArray)

            file.writeText(rootJson.toString(2))
            file.absolutePath
        }
    }

    suspend fun importBackup(): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val root = context.getExternalFilesDir("backup") ?: context.filesDir
            val file = File(root, "relaxmusic_backup.json")
            require(file.exists()) { "未找到备份文件: ${file.absolutePath}" }

            val json = JSONObject(file.readText())
            val settings = json.optJSONArray("settings") ?: JSONArray()
            for (i in 0 until settings.length()) {
                val item = settings.getJSONObject(i)
                settingsDao.put(SettingsEntity(item.getString("key"), item.getString("value")))
            }

            playlistDao.clearAllPlaylists()

            val idMap = mutableMapOf<Long, Long>()
            val playlists = json.optJSONArray("playlists") ?: JSONArray()
            for (i in 0 until playlists.length()) {
                val item = playlists.getJSONObject(i)
                val oldId = item.getLong("id")
                val newId = playlistDao.insertPlaylist(PlaylistEntity(name = item.getString("name")))
                idMap[oldId] = newId
            }

            val playlistSongs = json.optJSONArray("playlistSongs") ?: JSONArray()
            for (i in 0 until playlistSongs.length()) {
                val item = playlistSongs.getJSONObject(i)
                val newPlaylistId = idMap[item.getLong("playlistId")] ?: continue
                playlistDao.addSongToPlaylist(
                    PlaylistSongEntity(
                        playlistId = newPlaylistId,
                        songId = item.getString("songId")
                    )
                )
            }

            file.absolutePath
        }
    }
}
