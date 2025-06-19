package com.example.screentimetracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.screentimetracker.services.AppUsageTrackingService
import com.example.screentimetracker.utils.PermissionUtils

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            Log.d(TAG, "Boot completed event received.")
            // Check for usage stats permission before starting service
            if (PermissionUtils.hasUsageStatsPermission(context)) {
                val serviceIntent = Intent(context, AppUsageTrackingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d(TAG, "AppUsageTrackingService started on boot.")
            } else {
                Log.w(TAG, "Usage stats permission not granted. Service not started on boot.")
                // Optionally, schedule a reminder notification or handle this case
            }
        }
    }
}
