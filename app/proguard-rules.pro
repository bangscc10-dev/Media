# ---- Media3 / ExoPlayer ----
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**
# Keep our DB entities/daos explicitly
-keep class com.media.app.MediaOverride { *; }
-keep class com.media.app.PlayHistory { *; }
-keep class com.media.app.OverrideDao { *; }
-keep class com.media.app.HistoryDao { *; }
-keep class com.media.app.PlaybackPosition { *; }
-keep class com.media.app.PositionDao { *; }
-keep class com.media.app.OverrideDatabase { *; }

# ---- SplashScreen (uses reflection for animated icon) ----
-keep class androidx.core.splashscreen.** { *; }
-dontwarn androidx.core.splashscreen.**

# ---- DataStore ----
-keep class androidx.datastore.*.** { *; }
-dontwarn androidx.datastore.**

# ---- Kotlin coroutines / serialization safety ----
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ---- Our data models referenced across the app ----
-keep class com.media.app.AppMediaItem { *; }
-keep class com.media.app.MediaSettings { *; }
-keep enum com.media.app.** { *; }

# ---- Compose (generally safe, but keep runtime) ----
-dontwarn androidx.compose.**

# Keep Parcelable/enum valueOf used by state restoration
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
