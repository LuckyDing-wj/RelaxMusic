package com.relaxmusic.app.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.relaxmusic.app.domain.model.Song
import com.relaxmusic.app.utils.FileTypeUtils
import java.security.MessageDigest
import java.util.Locale

class DirectoryScanner {
    private val metadataReader = MetadataReader()

    fun scan(context: Context, treeUri: Uri): List<Song> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val result = mutableListOf<Song>()
        walk(context, root, result)
        return result
    }

    private fun walk(context: Context, file: DocumentFile, result: MutableList<Song>) {
        file.listFiles().forEach { child ->
            when {
                child.isDirectory -> walk(context, child, result)
                child.isFile && FileTypeUtils.isSupportedAudioFile(child.name) -> {
                    result += buildSong(context, child)
                }
            }
        }
    }

    private fun buildSong(context: Context, file: DocumentFile): Song {
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
            size = file.length(),
            modifiedAt = file.lastModified()
        )
    }

    private fun sha1(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return bytes.joinToString("") { String.format(Locale.US, "%02x", it) }
    }
}
