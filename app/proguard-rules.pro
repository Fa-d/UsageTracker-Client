-keep class dev.sadakat.screentimetracker.framework.workers.DailyAggregationWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}