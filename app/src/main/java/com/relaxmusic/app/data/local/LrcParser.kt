package com.relaxmusic.app.data.local

import com.relaxmusic.app.domain.model.LyricLine

class LrcParser {
    private val timeRegex = Regex("\\[(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")
    private val metadataRegex = Regex("^\\[(ti|ar|al|by|offset):.*]$", RegexOption.IGNORE_CASE)
    private val offsetRegex = Regex("^\\[offset:([+-]?\\d+)]$", RegexOption.IGNORE_CASE)

    fun parse(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        var globalOffsetMs = 0L

        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach

            offsetRegex.matchEntire(line)?.let { match ->
                globalOffsetMs = match.groupValues[1].toLongOrNull() ?: 0L
                return@forEach
            }

            if (metadataRegex.matches(line)) return@forEach

            val matches = timeRegex.findAll(line).toList()
            if (matches.isEmpty()) return@forEach

            val text = line.replace(timeRegex, "").trim().ifBlank { "..." }
            matches.forEach { match ->
                val hours = match.groupValues[1].toLongOrNull() ?: 0L
                val minutes = match.groupValues[2].toLongOrNull() ?: 0L
                val seconds = match.groupValues[3].toLongOrNull() ?: 0L
                val fractionRaw = match.groupValues[4]
                val fractionMs = when (fractionRaw.length) {
                    1 -> fractionRaw.toLongOrNull()?.times(100) ?: 0L
                    2 -> fractionRaw.toLongOrNull()?.times(10) ?: 0L
                    3 -> fractionRaw.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val timeMs = (hours * 3_600_000 + minutes * 60_000 + seconds * 1_000 + fractionMs + globalOffsetMs)
                    .coerceAtLeast(0L)
                lines += LyricLine(timeMs = timeMs, text = text)
            }
        }

        return lines
            .distinctBy { it.timeMs to it.text }
            .sortedBy { it.timeMs }
    }
}
