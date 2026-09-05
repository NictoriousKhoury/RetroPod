package com.retropod.player.data.media

/**
 * SPOTISAVER / SpotiDownloader filenames (and MediaStore titles copied from them)
 * are cleaned so the UI shows real artist/title instead of junk tags.
 */
object FilenameMetadata {

    private val junkAlbum = setOf("", "<unknown>", "unknown", "unknown album", "unknown artist")

    fun parse(
        fileName: String,
        taggedTitle: String,
        taggedArtist: String,
        taggedAlbum: String
    ): Triple<String, String, String> {
        val stem = fileName
            .removeSuffix(".mp3")
            .removeSuffix(".MP3")
            .replace(Regex("\\s*\\(\\d+\\)\\s*$"), "")

        val fromFile = parseStem(stem)
        val titleLooksLikeFile = taggedTitle.contains("SPOTISAVER", true) ||
            taggedTitle.contains("SpotiDownloader", true) ||
            taggedTitle.equals(stem, true) ||
            taggedTitle.equals(fileName, true)

        val artistLooksLikeFile = taggedArtist.contains("SPOTISAVER", true) ||
            taggedArtist.contains("SpotiDownloader", true) ||
            taggedArtist.equals("<unknown>", true) ||
            taggedArtist.isBlank()

        val title = stripEdition(
            if (titleLooksLikeFile) fromFile.first else stripJunk(taggedTitle)
        )
        val artist = if (artistLooksLikeFile) fromFile.second else stripJunk(taggedArtist).ifBlank { fromFile.second }
        val albumRaw = stripJunk(taggedAlbum)
        val albumJunk = albumRaw.lowercase() in junkAlbum ||
            albumRaw.contains("SPOTISAVER", true) ||
            albumRaw.equals(title, true) ||
            albumRaw.equals(stem, true)
        val album = if (albumJunk) artist else albumRaw

        return Triple(title.ifBlank { stem }, artist.ifBlank { "Unknown Artist" }, album.ifBlank { artist })
    }

    private fun parseStem(raw: String): Pair<String, String> {
        var stem = stripJunk(raw)
        // "SpotiDownloader.com - Title - Artist"
        if (stem.startsWith("SpotiDownloader.com -", true)) {
            stem = stem.substringAfter(" - ").trim()
            val lastDash = stem.lastIndexOf(" - ")
            if (lastDash > 0) {
                val title = stem.substring(0, lastDash).trim()
                val artist = stem.substring(lastDash + 3).trim()
                return title to artist.ifBlank { "Unknown Artist" }
            }
        }
        // "Artist - Title"
        val dash = stem.indexOf(" - ")
        if (dash > 0) {
            val artist = stem.substring(0, dash).trim()
            val title = stem.substring(dash + 3).trim()
            return title to artist
        }
        return stem to "Unknown Artist"
    }

    private fun stripJunk(s: String): String =
        s.replace("(SPOTISAVER)", "", ignoreCase = true)
            .replace("SPOTISAVER", "", ignoreCase = true)
            .replace("SpotiDownloader.com", "", ignoreCase = true)
            .replace(Regex("\\s{2,}"), " ")
            .trim(' ', '-', '.', '_')

    /**
     * Drops remaster / mix / version edition tags from a title.
     * Keeps remixes ("Seeb Remix") and distinct covers ("Jazz Version").
     */
    private fun stripEdition(raw: String): String {
        var s = raw.trim()
        var prev: String
        do {
            prev = s
            s = editionTail.replace(s, "").trim(' ', '-', '–', '—', '.', '_')
        } while (s != prev && s.isNotEmpty())
        return s
    }

    private val editionTail = Regex(
        """(?ix)
        \s*
        (?:[-–—]\s*|[(\[]\s*)
        (?:
            (?:(?:19|20)\d{2}\s+)?(?:digital\s+)?remaster(?:ed)?(?:\s+(?:19|20)\d{2})?
            |
            (?:19|20)\d{2}\s+(?:stereo\s+)?mix
            |
            stereo\s+mix(?:\s+remaster(?:ed)?)?(?:\s+(?:19|20)\d{2})?
            |
            original\s+mix
            |
            radio\s+mix
            |
            spike\s+mix
            |
            radio\s+edit
            |
            (?:(?:19|20)\d{2}\s+)?single\s+version
            |
            short\s+version
            |
            version\s+revisited
            |
            live
            |
            mono
        )
        \s*[)\]]?
        \s*$
        """
    )
}
