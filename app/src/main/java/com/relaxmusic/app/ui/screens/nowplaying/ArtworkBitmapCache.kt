package com.relaxmusic.app.ui.screens.nowplaying

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.relaxmusic.app.data.local.EmbeddedArtworkReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class ArtworkBitmapCache(
    private val artworkReader: EmbeddedArtworkReader = EmbeddedArtworkReader()
) {
    private val cache = mutableMapOf<String, Bitmap?>()

    suspend fun load(context: Context, songUri: String?, targetSizePx: Int): Bitmap? {
        if (songUri.isNullOrBlank()) return null

        val cacheKey = "$songUri#$targetSizePx"
        if (cache.containsKey(cacheKey)) {
            return cache[cacheKey]
        }

        val bitmap = withContext(Dispatchers.IO) {
            val bytes = artworkReader.read(context, songUri) ?: return@withContext null

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

            val sampleWidth = if (bounds.outWidth > 0) bounds.outWidth / targetSizePx else 1
            val sampleHeight = if (bounds.outHeight > 0) bounds.outHeight / targetSizePx else 1
            val inSampleSize = max(1, max(sampleWidth, sampleHeight))

            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size,
                BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            )
        }

        cache[cacheKey] = bitmap
        return bitmap
    }
}
