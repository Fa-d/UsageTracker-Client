-keep class dev.sadakat.screentimetracker.workers.DailyAggregationWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Keep TensorFlow Lite classes
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep Google J2ObjC annotations
-dontwarn com.google.j2objc.annotations.**

# Keep Guava classes used by the app
-keep class com.google.common.** { *; }
-dontwarn com.google.common.**