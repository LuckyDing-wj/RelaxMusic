package com.relaxmusic.app.data.local

import com.relaxmusic.app.domain.model.LyricLine
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset

class EmbeddedLyricsReader(
    private val lrcParser: LrcParser = LrcParser()
) {
    fun read(inputStream: InputStream): List<LyricLine> {
        val signature = inputStream.readExactlyOrNull(4) ?: return emptyList()

        return when {
            signature.copyOfRange(0, 3).contentEquals(ID3_SIGNATURE) -> {
                val remainder = inputStream.readExactlyOrNull(6) ?: return emptyList()
                parseId3(signature + remainder, inputStream)
            }
            signature.contentEquals(FLAC_SIGNATURE) -> parseFlac(inputStream)
            else -> emptyList()
        }
    }

    private fun parseId3(header: ByteArray, inputStream: InputStream): List<LyricLine> {
        val majorVersion = header[3].toInt() and 0xFF
        if (majorVersion !in setOf(3, 4)) return emptyList()

        val flags = header[5].toInt() and 0xFF
        val tagSize = decodeSyncSafeInt(header, 6)
        val tagBytes = inputStream.readExactlyOrNull(tagSize) ?: return emptyList()
        val body = if ((flags and FLAG_UNSYNCHRONISATION) != 0) removeUnsynchronisation(tagBytes) else tagBytes
        val frameStart = skipExtendedHeader(body, majorVersion, flags)
        if (frameStart >= body.size) return emptyList()

        return parseFrames(body, frameStart, majorVersion)
    }

    private fun parseFlac(inputStream: InputStream): List<LyricLine> {
        while (true) {
            val blockHeader = inputStream.readExactlyOrNull(4) ?: return emptyList()
            val isLastBlock = (blockHeader[0].toInt() and 0x80) != 0
            val blockType = blockHeader[0].toInt() and 0x7F
            val blockLength = ((blockHeader[1].toInt() and 0xFF) shl 16) or
                ((blockHeader[2].toInt() and 0xFF) shl 8) or
                (blockHeader[3].toInt() and 0xFF)

            val blockData = inputStream.readExactlyOrNull(blockLength) ?: return emptyList()
            if (blockType == FLAC_BLOCK_VORBIS_COMMENT) {
                val lyrics = parseVorbisComment(blockData)
                if (lyrics.isNotEmpty()) return lyrics
            }

            if (isLastBlock) return emptyList()
        }
    }

    private fun parseFrames(tagBody: ByteArray, startOffset: Int, majorVersion: Int): List<LyricLine> {
        var offset = startOffset
        var unsynchronisedLyrics: List<LyricLine> = emptyList()

        while (offset < tagBody.size) {
            if (tagBody[offset] == 0.toByte()) break
            if (offset + FRAME_HEADER_SIZE > tagBody.size) break

            val frameId = tagBody.decodeToString(offset, offset + 4)
            if (!frameId.all { it in 'A'..'Z' || it in '0'..'9' }) break

            val frameSize = if (majorVersion == 4) {
                decodeSyncSafeInt(tagBody, offset + 4)
            } else {
                decodeInt(tagBody, offset + 4)
            }
            if (frameSize <= 0) {
                offset += FRAME_HEADER_SIZE
                continue
            }

            val frameDataStart = offset + FRAME_HEADER_SIZE
            val frameDataEnd = frameDataStart + frameSize
            if (frameDataEnd > tagBody.size) break

            val frameData = tagBody.copyOfRange(frameDataStart, frameDataEnd)
            when (frameId) {
                FRAME_ID_SYLT -> {
                    val synced = parseSyncedLyrics(frameData)
                    if (synced.isNotEmpty()) return synced
                }
                FRAME_ID_USLT -> {
                    if (unsynchronisedLyrics.isEmpty()) {
                        unsynchronisedLyrics = parseUnsynchronisedLyrics(frameData)
                    }
                }
            }

            offset = frameDataEnd
        }

        return unsynchronisedLyrics
    }

    private fun parseUnsynchronisedLyrics(frameData: ByteArray): List<LyricLine> {
        if (frameData.size < 4) return emptyList()
        val encoding = frameData[0].toInt() and 0xFF
        val textStart = findEncodedTerminator(frameData, offset = 4, encoding = encoding)
        if (textStart > frameData.size) return emptyList()

        val lyricsText = decodeText(frameData, textStart, frameData.size, encoding).trim()
        return parseLyricsText(lyricsText)
    }

    private fun parseSyncedLyrics(frameData: ByteArray): List<LyricLine> {
        if (frameData.size < 6) return emptyList()
        val encoding = frameData[0].toInt() and 0xFF
        val timestampFormat = frameData[4].toInt() and 0xFF
        val contentType = frameData[5].toInt() and 0xFF
        if (timestampFormat != TIMESTAMP_MS || contentType !in setOf(CONTENT_TYPE_OTHER, CONTENT_TYPE_LYRICS, CONTENT_TYPE_TEXT)) {
            return emptyList()
        }

        var offset = findEncodedTerminator(frameData, 6, encoding)
        if (offset > frameData.size) return emptyList()

        val lines = mutableListOf<LyricLine>()
        while (offset < frameData.size) {
            val textEnd = findEncodedTextEnd(frameData, offset, encoding) ?: break
            if (textEnd + 4 > frameData.size) break

            val text = decodeText(frameData, offset, textEnd, encoding).trim()
            val timestamp = decodeInt(frameData, textEnd)
            if (text.isNotBlank()) {
                lines += LyricLine(timeMs = timestamp.toLong().coerceAtLeast(0L), text = text)
            }
            offset = textEnd + 4
        }

        return lines
            .distinctBy { it.timeMs to it.text }
            .sortedBy { it.timeMs }
    }

    private fun parseVorbisComment(blockData: ByteArray): List<LyricLine> {
        if (blockData.size < 8) return emptyList()

        var offset = 0
        val vendorLength = decodeLittleEndianInt(blockData, offset)
        offset += 4
        if (vendorLength < 0 || offset + vendorLength > blockData.size) return emptyList()
        offset += vendorLength
        if (offset + 4 > blockData.size) return emptyList()

        val commentCount = decodeLittleEndianInt(blockData, offset)
        offset += 4
        if (commentCount < 0) return emptyList()

        var untimedLyrics: List<LyricLine> = emptyList()
        repeat(commentCount) {
            if (offset + 4 > blockData.size) return untimedLyrics
            val commentLength = decodeLittleEndianInt(blockData, offset)
            offset += 4
            if (commentLength < 0 || offset + commentLength > blockData.size) return untimedLyrics

            val rawComment = blockData.copyOfRange(offset, offset + commentLength).toString(Charsets.UTF_8)
            offset += commentLength

            val separatorIndex = rawComment.indexOf('=')
            if (separatorIndex <= 0) return@repeat

            val key = rawComment.substring(0, separatorIndex).uppercase()
            if (key !in SUPPORTED_VORBIS_LYRIC_KEYS) return@repeat

            val lyrics = parseLyricsText(rawComment.substring(separatorIndex + 1))
            if (lyrics.isEmpty()) return@repeat
            if (lyrics.first().timeMs < UNTIMED_BASE_MS) return lyrics
            if (untimedLyrics.isEmpty()) untimedLyrics = lyrics
        }

        return untimedLyrics
    }

    private fun parseLyricsText(text: String): List<LyricLine> {
        val lyricsText = text.trim()
        if (lyricsText.isBlank()) return emptyList()

        val parsedLrc = lrcParser.parse(lyricsText)
        if (parsedLrc.isNotEmpty()) return parsedLrc

        return lyricsText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapIndexed { index, line ->
                LyricLine(timeMs = UNTIMED_BASE_MS + index, text = line)
            }
            .toList()
    }

    private fun skipExtendedHeader(tagBody: ByteArray, majorVersion: Int, flags: Int): Int {
        if ((flags and FLAG_EXTENDED_HEADER) == 0) return 0
        if (tagBody.size < 4) return tagBody.size

        val extendedHeaderSize = if (majorVersion == 4) {
            decodeSyncSafeInt(tagBody, 0)
        } else {
            decodeInt(tagBody, 0) + 4
        }
        return extendedHeaderSize.coerceIn(0, tagBody.size)
    }

    private fun decodeText(bytes: ByteArray, start: Int, end: Int, encoding: Int): String {
        if (start >= end || start >= bytes.size) return ""
        val safeEnd = end.coerceAtMost(bytes.size)
        return bytes.copyOfRange(start, safeEnd).toString(charsetForEncoding(encoding))
            .removePrefix("\uFEFF")
            .trimEnd('\u0000')
    }

    private fun findEncodedTerminator(bytes: ByteArray, offset: Int, encoding: Int): Int {
        val textEnd = findEncodedTextEnd(bytes, offset, encoding) ?: return bytes.size + 1
        return when (encoding) {
            TEXT_ENCODING_UTF16, TEXT_ENCODING_UTF16_BE -> textEnd + 2
            else -> textEnd + 1
        }
    }

    private fun findEncodedTextEnd(bytes: ByteArray, offset: Int, encoding: Int): Int? {
        if (offset >= bytes.size) return bytes.size
        return when (encoding) {
            TEXT_ENCODING_UTF16, TEXT_ENCODING_UTF16_BE -> {
                var index = offset
                while (index + 1 < bytes.size) {
                    if (bytes[index] == 0.toByte() && bytes[index + 1] == 0.toByte()) return index
                    index += 2
                }
                null
            }
            else -> {
                var index = offset
                while (index < bytes.size) {
                    if (bytes[index] == 0.toByte()) return index
                    index += 1
                }
                null
            }
        }
    }

    private fun charsetForEncoding(encoding: Int): Charset = when (encoding) {
        TEXT_ENCODING_ISO_8859_1 -> Charsets.ISO_8859_1
        TEXT_ENCODING_UTF16 -> Charsets.UTF_16
        TEXT_ENCODING_UTF16_BE -> Charsets.UTF_16BE
        TEXT_ENCODING_UTF8 -> Charsets.UTF_8
        else -> Charsets.ISO_8859_1
    }

    private fun decodeInt(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
    }

    private fun decodeSyncSafeInt(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0x7F) shl 21) or
            ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
            (bytes[offset + 3].toInt() and 0x7F)
    }

    private fun decodeLittleEndianInt(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return -1
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun removeUnsynchronisation(bytes: ByteArray): ByteArray {
        val output = ByteArrayOutputStream(bytes.size)
        var index = 0
        while (index < bytes.size) {
            val current = bytes[index]
            if (current == 0xFF.toByte() && index + 1 < bytes.size && bytes[index + 1] == 0.toByte()) {
                output.write(0xFF)
                index += 2
            } else {
                output.write(current.toInt())
                index += 1
            }
        }
        return output.toByteArray()
    }

    private fun InputStream.readExactlyOrNull(length: Int): ByteArray? {
        val result = ByteArray(length)
        var totalRead = 0
        while (totalRead < length) {
            val read = read(result, totalRead, length - totalRead)
            if (read <= 0) return null
            totalRead += read
        }
        return result
    }

    private companion object {
        val ID3_SIGNATURE = byteArrayOf('I'.code.toByte(), 'D'.code.toByte(), '3'.code.toByte())
        val FLAC_SIGNATURE = byteArrayOf('f'.code.toByte(), 'L'.code.toByte(), 'a'.code.toByte(), 'C'.code.toByte())
        const val FRAME_HEADER_SIZE = 10
        const val FLAG_UNSYNCHRONISATION = 0x80
        const val FLAG_EXTENDED_HEADER = 0x40
        const val FRAME_ID_USLT = "USLT"
        const val FRAME_ID_SYLT = "SYLT"
        const val FLAC_BLOCK_VORBIS_COMMENT = 4
        const val TEXT_ENCODING_ISO_8859_1 = 0
        const val TEXT_ENCODING_UTF16 = 1
        const val TEXT_ENCODING_UTF16_BE = 2
        const val TEXT_ENCODING_UTF8 = 3
        const val TIMESTAMP_MS = 2
        const val CONTENT_TYPE_OTHER = 0
        const val CONTENT_TYPE_LYRICS = 1
        const val CONTENT_TYPE_TEXT = 2
        const val UNTIMED_BASE_MS = 315_576_000_000L
        val SUPPORTED_VORBIS_LYRIC_KEYS = setOf(
            "LYRICS",
            "LYRIC",
            "SYNCEDLYRICS",
            "SYNCED LYRICS",
            "UNSYNCEDLYRICS",
            "UNSYNCED LYRICS"
        )
    }
}
