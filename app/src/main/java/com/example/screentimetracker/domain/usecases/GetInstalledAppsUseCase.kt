package com.example.screentimetracker.domain.usecases

import android.app.Application // For PackageManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppInfo(
    val packageName: String,
    val appName: String
    // val icon: Drawable? // Icon loading can be complex and resource-intensive here, usually done async in UI
)

class GetInstalledAppsUseCase @Inject constructor(
    private val application: Application // Inject Application context
) {
    suspend operator fun invoke(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = application.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        }

        installedApps.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val appName = pm.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString()
            AppInfo(
                packageName = packageName,
                appName = appName
            )
        }.distinctBy { it.packageName } // Ensure unique apps
            .sortedBy { it.appName.lowercase() } // Sort alphabetically by app name
    }
}
