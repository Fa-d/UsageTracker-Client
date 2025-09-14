# Add project specific ProGuard rules here.

# Keep Room entities
-keep @androidx.room.Entity class * {
    <fields>;
}

# Keep Room DAOs
-keep @androidx.room.Dao class * {
    *;
}

# Keep database class
-keep class * extends androidx.room.RoomDatabase {
    *;
}

# Keep type converters
-keep class * {
    @androidx.room.TypeConverter *;
}

# Keep kotlinx.datetime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**