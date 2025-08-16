package com.example.screentimetracker.ai

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIFeatureManager(
    private val context: Context
) {
    companion object {
        private const val AI_MODULE_NAME = "ai_insights"
    }

    private val splitInstallManager: SplitInstallManager by lazy {
        SplitInstallManagerFactory.create(context)
    }

    private val _downloadState = MutableStateFlow(AIDownloadState.NOT_DOWNLOADED)
    val downloadState: StateFlow<AIDownloadState> = _downloadState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val splitInstallListener = SplitInstallStateUpdatedListener { state ->
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                _downloadState.value = AIDownloadState.DOWNLOADING
                val progress = (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toInt()
                _downloadProgress.value = progress
            }
            SplitInstallSessionStatus.INSTALLING -> {
                _downloadState.value = AIDownloadState.INSTALLING
                _downloadProgress.value = 100
            }
            SplitInstallSessionStatus.INSTALLED -> {
                _downloadState.value = AIDownloadState.READY
                _downloadProgress.value = 100
            }
            SplitInstallSessionStatus.FAILED -> {
                _downloadState.value = AIDownloadState.FAILED
                _downloadProgress.value = 0
            }
            SplitInstallSessionStatus.CANCELED -> {
                _downloadState.value = AIDownloadState.NOT_DOWNLOADED
                _downloadProgress.value = 0
            }
        }
    }

    init {
        splitInstallManager.registerListener(splitInstallListener)
        checkAIAvailability()
    }

    fun isAIAvailable(): Boolean {
        return splitInstallManager.installedModules.contains(AI_MODULE_NAME)
    }

    fun downloadAIFeatures() {
        if (isAIAvailable()) {
            _downloadState.value = AIDownloadState.READY
            return
        }

        val request = SplitInstallRequest.newBuilder()
            .addModule(AI_MODULE_NAME)
            .build()

        splitInstallManager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                // Download started successfully
                _downloadState.value = AIDownloadState.DOWNLOADING
            }
            .addOnFailureListener { exception ->
                // Download failed
                _downloadState.value = AIDownloadState.FAILED
            }
    }

    fun cancelDownload() {
        // Cancel any ongoing download
        splitInstallManager.deferredUninstall(listOf(AI_MODULE_NAME))
        _downloadState.value = AIDownloadState.NOT_DOWNLOADED
        _downloadProgress.value = 0
    }

    private fun checkAIAvailability() {
        _downloadState.value = if (isAIAvailable()) {
            AIDownloadState.READY
        } else {
            AIDownloadState.NOT_DOWNLOADED
        }
    }

    fun uninstallAIFeatures() {
        splitInstallManager.deferredUninstall(listOf(AI_MODULE_NAME))
        _downloadState.value = AIDownloadState.NOT_DOWNLOADED
        _downloadProgress.value = 0
    }

    fun cleanup() {
        splitInstallManager.unregisterListener(splitInstallListener)
    }
}

enum class AIDownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    INSTALLING,
    READY,
    FAILED
}