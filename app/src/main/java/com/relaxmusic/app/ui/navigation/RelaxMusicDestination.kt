package com.relaxmusic.app.ui.navigation

import android.net.Uri
import com.relaxmusic.app.ui.components.TopLevelDestination

sealed class RelaxMusicDestination(val route: String) {
    object Home : RelaxMusicDestination("home")
    object NowPlaying : RelaxMusicDestination("player")
    object FullLibrary : RelaxMusicDestination("full-library")
    object Albums : RelaxMusicDestination("albums")
    object Artists : RelaxMusicDestination("artists")
    object ListsHub : RelaxMusicDestination("lists")
    object Playlists : RelaxMusicDestination("playlists")
    object Favorites : RelaxMusicDestination("favorites")
    object History : RelaxMusicDestination("history")
    object Settings : RelaxMusicDestination("settings")

    object AlbumDetail : RelaxMusicDestination("album/{albumName}/{albumArtist}") {
        const val BASE_ROUTE = "album"
        const val ALBUM_NAME_ARG = "albumName"
        const val ALBUM_ARTIST_ARG = "albumArtist"

        fun createRoute(albumName: String, albumArtist: String): String {
            return "$BASE_ROUTE/${Uri.encode(albumName)}/${Uri.encode(albumArtist)}"
        }
    }

    object ArtistDetail : RelaxMusicDestination("artist/{artistName}") {
        const val BASE_ROUTE = "artist"
        const val ARTIST_NAME_ARG = "artistName"

        fun createRoute(artistName: String): String {
            return "$BASE_ROUTE/${Uri.encode(artistName)}"
        }
    }

    object PlaylistDetail : RelaxMusicDestination("playlist/{playlistId}") {
        const val BASE_ROUTE = "playlist"
        const val PLAYLIST_ID_ARG = "playlistId"

        fun createRoute(playlistId: Long): String {
            return "$BASE_ROUTE/$playlistId"
        }
    }

    companion object {
        fun topLevelDestinationForRoute(route: String?): TopLevelDestination? {
            return when {
                route == null -> null
                route == Home.route -> TopLevelDestination.HOME
                route == NowPlaying.route -> TopLevelDestination.PLAYER

                route == FullLibrary.route ||
                    route == Albums.route ||
                    route == Artists.route ||
                    route == AlbumDetail.route ||
                    route == ArtistDetail.route ||
                    route.startsWith("${AlbumDetail.BASE_ROUTE}/") ||
                    route.startsWith("${ArtistDetail.BASE_ROUTE}/") -> TopLevelDestination.HOME

                route == ListsHub.route ||
                    route == Playlists.route ||
                    route == Favorites.route ||
                    route == History.route ||
                    route == PlaylistDetail.route ||
                    route.startsWith("${PlaylistDetail.BASE_ROUTE}/") -> TopLevelDestination.LISTS

                route == Settings.route -> TopLevelDestination.HOME
                else -> null
            }
        }
    }
}
