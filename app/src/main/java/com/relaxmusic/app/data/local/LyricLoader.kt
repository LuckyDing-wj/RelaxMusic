package com.relaxmusic.app.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
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
        Log.d(TAG, "load: fileName=$fileName, songUri=$songUri")
        val document = DocumentFile.fromSingleUri(context, Uri.parse(songUri))
        if (document == null) {
            Log.w(TAG, "load: document is null for uri=$songUri")
            return@withContext emptyList()
        }

        // Try external .lrc file first
        val parent = document.parentFile
        if (parent != null) {
            val targetName = fileName.substringBeforeLast('.', fileName) + ".lrc"
            val lyricFile = parent.listFiles().firstOrNull { it.isFile && it.name.equals(targetName, ignoreCase = true) }
            Log.d(TAG, "load: looking for external lrc: $targetName, found=${lyricFile != null}")

            val externalLyrics = lyricFile?.let {
                runCatching {
                    context.contentResolver.openInputStream(it.uri)?.bufferedReader()?.use { reader ->
                        parser.parse(reader.readText())
                    }.orEmpty()
                }.getOrElse { emptyList() }
            }.orEmpty()
            if (externalLyrics.isNotEmpty()) {
                Log.d(TAG, "load: found external lyrics, count=${externalLyrics.size}")
                return@withContext externalLyrics
            }
        } else {
            Log.d(TAG, "load: parentFile is null, skipping external lrc search")
        }

        // Try embedded lyrics
        Log.d(TAG, "load: trying embedded lyrics")
        val embeddedLyrics = runCatching {
            context.contentResolver.openInputStream(document.uri)?.use { input ->
                embeddedLyricsReader.read(input)
            }.orEmpty()
        }.onFailure { e ->
            Log.e(TAG, "load: error reading embedded lyrics", e)
        }.getOrDefault(emptyList())

        Log.d(TAG, "load: embedded lyrics count=${embeddedLyrics.size}")
        embeddedLyrics
    }

    companion object {
        private const val TAG = "LyricLoader"
    }
}
