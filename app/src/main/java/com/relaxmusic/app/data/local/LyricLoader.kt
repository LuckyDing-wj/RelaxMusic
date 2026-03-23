package com.relaxmusic.app.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.relaxmusic.app.domain.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SongLyricLoader {
    suspend fun load(context: Context, songUri: String, fileName: String): List<LyricLine>
}

class LyricLoader(
    private val parser: LrcParser = LrcParser(),
    private val embeddedLyricsReader: EmbeddedLyricsReader = EmbeddedLyricsReader()
) : SongLyricLoader {
    override suspend fun load(context: Context, songUri: String, fileName: String): List<LyricLine> = withContext(Dispatchers.IO) {
        val document = DocumentFile.fromSingleUri(context, Uri.parse(songUri)) ?: return@withContext emptyList()
        val parent = document.parentFile ?: return@withContext emptyList()
        val targetName = fileName.substringBeforeLast('.', fileName) + ".lrc"
        val lyricFile = parent.listFiles().firstOrNull { it.isFile && it.name.equals(targetName, ignoreCase = true) }

        val externalLyrics = lyricFile?.let {
            runCatching {
                context.contentResolver.openInputStream(it.uri)?.bufferedReader()?.use { reader ->
                    parser.parse(reader.readText())
                }.orEmpty()
            }.getOrElse { emptyList() }
        }.orEmpty()
        if (externalLyrics.isNotEmpty()) return@withContext externalLyrics

        runCatching {
            context.contentResolver.openInputStream(document.uri)?.use { input ->
                embeddedLyricsReader.read(input)
            }.orEmpty()
        }.getOrElse { emptyList() }
    }
}
