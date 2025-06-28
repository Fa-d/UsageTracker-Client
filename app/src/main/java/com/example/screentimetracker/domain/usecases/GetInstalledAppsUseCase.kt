package com.example.screentimetracker.domain.usecases

import android.app.Application // For PackageManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        installedApps.mapNotNull { appInfo ->
            AppInfo(
                packageName = appInfo.packageName,
                appName = pm.getApplicationLabel(appInfo).toString()
            )
        }.sortedBy { it.appName.lowercase() } // Sort alphabetically by app name
    }
}
