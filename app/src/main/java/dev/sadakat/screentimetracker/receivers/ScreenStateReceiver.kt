package dev.sadakat.screentimetracker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.sadakat.screentimetracker.services.AppUsageTrackingService

class ScreenStateReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "ScreenStateReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Log.d(TAG, "Screen off event received.")
                val serviceIntent = Intent(context, AppUsageTrackingService::class.java)
                serviceIntent.action = AppUsageTrackingService.ACTION_HANDLE_SCREEN_OFF
                context.startService(serviceIntent) // Or startForegroundService if needed
            }
            // Intent.ACTION_USER_PRESENT could also be handled here if consolidating
        }
    }
}