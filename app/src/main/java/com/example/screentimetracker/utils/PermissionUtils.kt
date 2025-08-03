package com.example.screentimetracker.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

object PermissionUtils {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false // Return false if AppOpsManager is not available
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            context.applicationInfo.uid,
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        // Optional: Add package name to intent to directly open settings for this app,
        // but ACTION_USAGE_ACCESS_SETTINGS usually takes user to the list.
        // intent.data = Uri.parse("package:${context.packageName}")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context is androidx.activity.ComponentActivity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    0 // Request code, not used here but required
                )
            }
        }
    }
}
