# RetroPod — iOS 6 style music player (Android / Galaxy S25)

A native Kotlin + Jetpack Compose + Media3 music player skinned like the iPod
Touch 2nd-gen / iOS 6 Music app, with a modern "Up Next" queue, Cover Flow,
search, an EQ, a home-screen widget, and your curated `D:\Music`
genre/decade/vibe playlists.

## What's implemented

| Phase | Feature | Where |
|------|----------|-------|
| 0 | `.m3u8` export from `manifest.csv` | `D:\Music\_export_m3u.ps1`, `_copy_to_phone.ps1` |
| 1 | Library scan + Songs/Artists/Albums | `data/media/MediaStoreScanner.kt`, `ui/screens/*` |
| 2 | Media3 playback service, mini-player, lock-screen/Bluetooth, resume | `playback/*` |
| 3 | iOS 6 skin: tab bar, table rows, Now Playing + scrubber | `ui/theme/*`, `ui/components/*`, `NowPlayingScreen.kt` |
| 4 | Up Next queue: drag-reorder, swipe-remove, play next/add | `QueueScreen.kt`, `PlaybackConnection.kt` |
| 5 | Import curated playlists + create/edit user playlists (Room) | `data/playlist/*`, `data/db/*`, `PlaylistsScreen.kt` |
| 6 | Cover Flow (3D rotation + reflection) | `CoverFlowScreen.kt` |
| 7 | Search across songs/artists/albums/playlists | `SearchScreen.kt` |
| 8 | Gapless (ExoPlayer default), system EQ, Glance widget | `EqualizerScreen.kt`, `widget/RetroPodWidget.kt` |

## Phase 0 — get your library onto the phone (run on the PC)

Already generated: `D:\Music\Playlists-M3U\*.m3u8` (24 playlists) via
`_export_m3u.ps1`. Each playlist lists the original mp3 file names, so no audio
is duplicated.

Copy to the phone (either option):
- **MTP (drag & drop):** copy `D:\Music\*.mp3` and the whole
  `D:\Music\Playlists-M3U` folder into `Internal storage > Music > RetroPod`.
- **adb:** enable USB debugging, then run `D:\Music\_copy_to_phone.ps1`.

The app matches `.m3u8` entries to scanned songs by file name, so the exact
folder only needs to be somewhere under `Music/`.

## Build & run

1. Open `D:\Projects\RetroPod` in Android Studio (Ladybug or newer).
2. Let Gradle sync. First run generates the Gradle wrapper if needed
   (`gradle wrapper` or use the IDE).
3. Enable USB debugging on the S25, plug in, and Run — or `./gradlew installDebug`.
4. Grant the audio (and notifications) permission on first launch.

### Version note
`gradle/libs.versions.toml` pins **known-good stable** versions
(AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, Media3 1.5.1, Room 2.6.1,
Hilt 2.52). The original plan referenced aspirational future versions; these
real ones resolve today. `minSdk 33` keeps us on `READ_MEDIA_AUDIO` only. Bump
versions in the catalog if you want newer.

## Architecture

- Single-activity Compose app. UI talks to playback through a `MediaController`
  bound to `PlaybackService : MediaLibraryService` (ExoPlayer).
- The queue is Media3's timeline surfaced as an editable "Up Next" screen
  (`setMediaItems` / `addMediaItem` / `moveMediaItem` / `removeMediaItem`).
- Room stores user playlists, favourites, play counts, and the resume snapshot.
- `MediaStoreScanner` builds Songs/Albums/Artists; `M3uPlaylistImporter` loads
  the curated `.m3u8` buckets and matches them to scanned songs by file name.

## Notes / possible next polish
- Drop a Helvetica Neue `.ttf` into `app/src/main/res/font` and point
  `ui/theme/Type.kt` at it for a pixel-accurate iOS 6 typeface.
- The EQ binds to the global output mix (session 0); most OEMs honour it.
- The Glance widget currently launches the app; it can be extended to show the
  live now-playing track and transport buttons via the session token.
