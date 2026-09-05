package com.retropod.player.ui.nav

object Routes {
    const val PLAYLISTS = "playlists"
    const val ARTISTS = "artists"
    const val SONGS = "songs"
    const val ALBUMS = "albums"
    const val MORE = "more"

    const val ALBUM_DETAIL = "album/{albumId}"
    const val ARTIST_DETAIL = "artist/{artistName}"
    const val ARTIST_SONGS = "artist/{artistName}/songs"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}/{imported}"
    const val ADD_TO_PLAYLIST = "addtoplaylist/{playlistId}"

    const val NOW_PLAYING = "nowplaying"
    const val QUEUE = "queue"
    const val SEARCH = "search"
    const val COVER_FLOW = "coverflow"
    const val SETTINGS = "settings"
    const val EQUALIZER = "equalizer"

    fun album(id: Long) = "album/$id"
    fun artist(name: String) = "artist/${java.net.URLEncoder.encode(name, "UTF-8")}"
    fun artistSongs(name: String) = "artist/${java.net.URLEncoder.encode(name, "UTF-8")}/songs"
    fun playlist(id: Long, imported: Boolean) = "playlist/$id/$imported"
    fun addToPlaylist(id: Long) = "addtoplaylist/$id"

    val topLevel = listOf(PLAYLISTS, ARTISTS, SONGS, ALBUMS)
}
