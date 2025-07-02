# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Room entities and DAOs
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase

# Keep Compose-related classes
-keep class androidx.compose.** { *; }

# General rules
-dontwarn kotlinx.coroutines.**
