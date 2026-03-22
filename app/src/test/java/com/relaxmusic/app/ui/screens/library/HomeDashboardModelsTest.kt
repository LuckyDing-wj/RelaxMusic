package com.relaxmusic.app.ui.screens.library

import com.google.common.truth.Truth.assertThat
import com.relaxmusic.app.domain.model.Song
import org.junit.Test

class HomeDashboardModelsTest {

    @Test
    fun buildHomeDashboardModel_limitsRecentPreviewToFiveSongs() {
        val songs = (1..7).map(::song)

        val model = buildHomeDashboardModel(
            libraryState = LibraryUiState(recentSongs = songs, totalSongCount = songs.size),
            currentSong = songs.first(),
            isPlaying = true,
            playbackProgress = 0.25f
        )

        assertThat(model.recentSongs.map { it.id }).containsExactly("1", "2", "3", "4", "5").inOrder()
    }

    @Test
    fun buildHomeDashboardModel_hidesRecentSectionWhenThereIsNoRecentOrHistoryContent() {
        val model = buildHomeDashboardModel(
            libraryState = LibraryUiState(totalSongCount = 0),
            currentSong = null,
            isPlaying = false,
            playbackProgress = 0f
        )

        assertThat(model.showRecentSection).isFalse()
        assertThat(model.recentSongs).isEmpty()
    }

    @Test
    fun buildHomeDashboardModel_keepsBrowseLibraryCopyStable() {
        val emptyLibraryModel = buildHomeDashboardModel(
            libraryState = LibraryUiState(totalSongCount = 0),
            currentSong = null,
            isPlaying = false,
            playbackProgress = 0f
        )
        val populatedLibraryModel = buildHomeDashboardModel(
            libraryState = LibraryUiState(totalSongCount = 12, recentSongs = listOf(song(1))),
            currentSong = song(2),
            isPlaying = true,
            playbackProgress = 0.5f
        )

        assertThat(emptyLibraryModel.libraryEntryTitle).isEqualTo("浏览曲库")
        assertThat(emptyLibraryModel.libraryEntrySubtitle).isEqualTo("全部歌曲、专辑、艺术家")
        assertThat(populatedLibraryModel.libraryEntryTitle).isEqualTo("浏览曲库")
        assertThat(populatedLibraryModel.libraryEntrySubtitle).isEqualTo("全部歌曲、专辑、艺术家")
    }

    private fun song(index: Int): Song {
        return Song(
            id = index.toString(),
            uri = "content://songs/$index",
            fileName = "song-$index.mp3",
            title = "Song $index",
            artist = "Artist $index",
            album = "Album $index",
            duration = 60_000L,
            size = 1_024L,
            modifiedAt = 1_700_000_000_000L + index
        )
    }
}
