# Add project specific ProGuard rules here.

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Coil classes for image loading
-keep class coil3.** { *; }
-dontwarn coil3.**

# Keep UI components and their constructors
-keep class dev.sadakat.screentimetracker.core.ui.** { *; }