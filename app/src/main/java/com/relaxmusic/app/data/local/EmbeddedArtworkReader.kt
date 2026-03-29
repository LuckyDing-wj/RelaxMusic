package com.relaxmusic.app.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

fun interface ArtworkMetadataRetrieverFactory {
    fun create(): ArtworkMetadataRetriever
}

interface ArtworkMetadataRetriever {
    fun setDataSource(context: Context?, songUri: String)
    fun embeddedPicture(): ByteArray?
    fun release()
}

class EmbeddedArtworkReader(
    private val retrieverFactory: ArtworkMetadataRetrieverFactory = ArtworkMetadataRetrieverFactory {
        AndroidArtworkMetadataRetriever()
    }
) {
    fun read(context: Context?, songUri: String?): ByteArray? {
        if (songUri.isNullOrBlank()) return null

        val retriever = retrieverFactory.create()
        return try {
            retriever.setDataSource(context, songUri)
            retriever.embeddedPicture()?.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}

private class AndroidArtworkMetadataRetriever : ArtworkMetadataRetriever {
    private val retriever = MediaMetadataRetriever()

    override fun setDataSource(context: Context?, songUri: String) {
        requireNotNull(context) { "Context is required to load embedded artwork." }
        retriever.setDataSource(context, Uri.parse(songUri))
    }

    override fun embeddedPicture(): ByteArray? = retriever.embeddedPicture

    override fun release() {
        retriever.release()
    }
}
