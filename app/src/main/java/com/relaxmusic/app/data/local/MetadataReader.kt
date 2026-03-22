package com.relaxmusic.app.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

data class SongMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long?
)

class MetadataReader {
    fun read(context: Context, uri: Uri): SongMetadata {
        val retriever = MediaMetadataRetriever()
        return runCatching {
            retriever.setDataSource(context, uri)
            SongMetadata(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            )
        }.getOrElse {
            SongMetadata(null, null, null, null)
        }.also {
            retriever.release()
        }
    }
}
