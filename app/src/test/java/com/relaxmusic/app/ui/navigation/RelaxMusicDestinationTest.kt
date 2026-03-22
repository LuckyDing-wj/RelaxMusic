package com.relaxmusic.app.ui.navigation

import com.google.common.truth.Truth.assertThat
import com.relaxmusic.app.ui.components.TopLevelDestination
import org.junit.Test

class RelaxMusicDestinationTest {

    @Test
    fun libraryHubRouteMapsToLibraryTopLevel() {
        assertThat(
            RelaxMusicDestination.topLevelDestinationForRoute(RelaxMusicDestination.LibraryHub.route)
        ).isEqualTo(TopLevelDestination.LIBRARY)
    }

    @Test
    fun fullLibraryRouteMapsToLibraryTopLevel() {
        assertThat(
            RelaxMusicDestination.topLevelDestinationForRoute(RelaxMusicDestination.FullLibrary.route)
        ).isEqualTo(TopLevelDestination.LIBRARY)
    }

    @Test
    fun albumDetailRouteMapsToLibraryTopLevel() {
        assertThat(
            RelaxMusicDestination.topLevelDestinationForRoute(
                RelaxMusicDestination.AlbumDetail.createRoute(
                    albumName = "Test Album",
                    albumArtist = "Test Artist"
                )
            )
        ).isEqualTo(TopLevelDestination.LIBRARY)
    }

    @Test
    fun listsHubRouteMapsToListsTopLevel() {
        assertThat(
            RelaxMusicDestination.topLevelDestinationForRoute(RelaxMusicDestination.ListsHub.route)
        ).isEqualTo(TopLevelDestination.LISTS)
    }

    @Test
    fun favoritesRouteMapsToListsTopLevel() {
        assertThat(
            RelaxMusicDestination.topLevelDestinationForRoute(RelaxMusicDestination.Favorites.route)
        ).isEqualTo(TopLevelDestination.LISTS)
    }

    @Test
    fun playlistDetailRouteMapsToListsTopLevel() {
        assertThat(
            RelaxMusicDestination.topLevelDestinationForRoute(
                RelaxMusicDestination.PlaylistDetail.createRoute(playlistId = 42L)
            )
        ).isEqualTo(TopLevelDestination.LISTS)
    }
}
