package com.relaxmusic.app.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.relaxmusic.app.data.db.entity.SongEntity
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.utils.FileTypeUtils
import java.security.MessageDigest
import java.util.Locale

class DirectoryScanner {
    private val metadataReader = MetadataReader()

    fun scan(
        context: Context,
        treeUri: Uri,
        existingById: Map<String, SongEntity> = emptyMap()
    ): List<Song> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val result = mutableListOf<Song>()
        walk(context, root, result, existingById)
        return result
    }

    private fun walk(
        context: Context,
        file: DocumentFile,
        result: MutableList<Song>,
        existingById: Map<String, SongEntity>
    ) {
        file.listFiles().forEach { child ->
            when {
                child.isDirectory -> walk(context, child, result, existingById)
                child.isFile && FileTypeUtils.isSupportedAudioFile(child.name) -> {
                    result += buildSong(context, child, existingById[sha1(child.uri.toString())])
                }
            }
        }
    }

    private fun buildSong(context: Context, file: DocumentFile, existing: SongEntity?): Song {
        val currentSize = file.length()
        val currentModifiedAt = file.lastModified()
        if (existing != null && existing.size == currentSize && existing.modifiedAt == currentModifiedAt) {
            return existing.toDomain()
        }

        val metadata = metadataReader.read(context, file.uri)
        val fileName = file.name.orEmpty()
        val fallbackTitle = fileName.substringBeforeLast('.', fileName)

        return Song(
            id = sha1(file.uri.toString()),
            uri = file.uri.toString(),
            fileName = fileName,
            title = metadata.title?.takeIf { it.isNotBlank() } ?: fallbackTitle,
            artist = metadata.artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist",
            album = metadata.album?.takeIf { it.isNotBlank() } ?: "Unknown Album",
            duration = metadata.durationMs ?: 0L,
            size = currentSize,
            modifiedAt = currentModifiedAt,
            isFavorite = existing?.isFavorite ?: false,
            lastPlayedAt = existing?.lastPlayedAt
        )
    }

    private fun SongEntity.toDomain(): Song = Song(
        id = id,
        uri = uri,
        fileName = fileName,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        size = size,
        modifiedAt = modifiedAt,
        isFavorite = isFavorite,
        lastPlayedAt = lastPlayedAt
    )

    private fun sha1(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return bytes.joinToString("") { String.format(Locale.US, "%02x", it) }
    }
}
