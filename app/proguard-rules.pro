# Keep Media3 session callbacks
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt / Dagger generated
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
