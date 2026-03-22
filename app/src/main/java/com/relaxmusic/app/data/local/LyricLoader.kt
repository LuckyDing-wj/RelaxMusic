package com.relaxmusic.app.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.relaxmusic.app.domain.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricLoader(
    private val parser: LrcParser = LrcParser()
) {
    suspend fun load(context: Context, songUri: String, fileName: String): List<LyricLine> = withContext(Dispatchers.IO) {
        val document = DocumentFile.fromSingleUri(context, Uri.parse(songUri)) ?: return@withContext emptyList()
        val parent = document.parentFile ?: return@withContext emptyList()
        val targetName = fileName.substringBeforeLast('.', fileName) + ".lrc"
        val lyricFile = parent.listFiles().firstOrNull { it.isFile && it.name.equals(targetName, ignoreCase = true) }
            ?: return@withContext emptyList()

        runCatching {
            context.contentResolver.openInputStream(lyricFile.uri)?.bufferedReader()?.use { reader ->
                parser.parse(reader.readText())
            }.orEmpty()
        }.getOrElse { emptyList() }
    }
}
