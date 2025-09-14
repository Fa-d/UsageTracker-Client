# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes for JSON serialization
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * { *; }

# Keep sealed classes
-keep class ** extends **$WhenMappings { *; }

# Keep kotlinx.datetime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**