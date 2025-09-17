package dev.sadakat.screentimetracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dev.sadakat.screentimetracker.services.SmartUsageTrackingService
import dev.sadakat.screentimetracker.utils.PermissionUtils

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            Log.d(TAG, "Boot completed event received.")
            // Check for usage stats permission before starting service
            if (PermissionUtils.hasUsageStatsPermission(context)) {
                SmartUsageTrackingService.ServiceController.startSmartTracking(context)
                Log.d(TAG, "SmartUsageTrackingService started on boot.")
            } else {
                Log.w(TAG, "Usage stats permission not granted. Service not started on boot.")
                // Optionally, schedule a reminder notification or handle this case
            }
        }
    }
}