
# Keep Google J2ObjC annotations
-dontwarn com.google.j2objc.annotations.**

# Keep Guava classes used by the app
-keep class com.google.common.** { *; }
-dontwarn com.google.common.**

# Aggressive size optimization
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove unused resources more aggressively
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

# Hilt optimization
-dontwarn dagger.hilt.**
-keep,allowobfuscation,allowshrinking class dagger.hilt.**
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.**

# Compose optimizations
-keep,allowobfuscation,allowshrinking class androidx.compose.**
-dontwarn androidx.compose.**

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}