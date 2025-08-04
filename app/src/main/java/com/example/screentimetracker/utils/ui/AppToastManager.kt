package com.example.screentimetracker.utils.ui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.screentimetracker.ui.MainActivity
import com.example.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface AppToastManager {
    fun showDissuasionToast(appName: String)
    fun bringAppToForeground(packageName: String)
}

@Singleton
class AppToastManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLogger: AppLogger
) : AppToastManager {

    companion object {
        private const val TAG = "AppToastManager"
    }

    override fun showDissuasionToast(appName: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Usage limit for $appName exceeded 3x. Taking a break!", Toast.LENGTH_LONG).show()
        }
    }

    override fun bringAppToForeground(packageName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        try {
            context.startActivity(intent)
            appLogger.d(TAG, "Attempted to bring MainActivity to front for $packageName.")
        } catch (e: Exception) {
            appLogger.e(TAG, "Could not start MainActivity for dissuasion: ${e.message}", e)
        }
    }
}
