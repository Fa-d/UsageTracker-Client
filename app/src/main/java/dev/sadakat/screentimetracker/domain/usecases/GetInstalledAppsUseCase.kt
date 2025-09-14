package dev.sadakat.screentimetracker.domain.usecases

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import javax.inject.Inject

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

class GetInstalledAppsUseCase @Inject constructor(
    private val application: Application
) {
    operator fun invoke(): List<InstalledApp> {
        val pm = application.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return installedApps
            .filter { app -> app.packageName != application.packageName }
            .map { app ->
                InstalledApp(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.appName }
    }
}