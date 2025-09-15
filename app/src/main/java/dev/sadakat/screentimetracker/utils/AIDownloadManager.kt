package dev.sadakat.screentimetracker.utils

import android.content.Context
import android.util.Log
import dev.sadakat.screentimetracker.ui.ai.AIDownloadState
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sadakat.screentimetracker.domain.habits.repository.DigitalPetRepository
import dev.sadakat.screentimetracker.utils.AIUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

data class AIDownloadProgress(
    val state: AIDownloadState,
    val progress: Int,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val downloadSpeed: String = "",
    val timeRemaining: String = "",
    val errorMessage: String? = null,
    val isRetryable: Boolean = false
)

@Singleton
class AIDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getContext(): Context = context
    private val _downloadProgress = MutableStateFlow(
        AIDownloadProgress(
            state = AIDownloadState.NOT_DOWNLOADED,
            progress = 0
        )
    )
    val downloadProgress: StateFlow<AIDownloadProgress> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private var aiFeatureManager: AIFeatureManagerWrapper? = null
    private var downloadStartTime: Long = 0L
    private var lastProgressUpdate: Long = 0L
    private var lastBytesDownloaded: Long = 0L

    fun initializeAIFeatureManager(): AIFeatureManagerWrapper {
        if (aiFeatureManager == null) {
            aiFeatureManager = AIFeatureManagerWrapper(context)
        }
        return aiFeatureManager!!
    }

    fun observeDownloadProgress(): StateFlow<AIDownloadProgress> {
        val manager = initializeAIFeatureManager()
        
        // Combine the AI feature manager's state with our enhanced progress
        return combine(
            manager.downloadState,
            manager.downloadProgress
        ) { state, progress ->
            updateDownloadProgress(state, progress)
        }.stateIn(
            scope = kotlinx.coroutines.GlobalScope,
            started = kotlinx.coroutines.flow.SharingStarted.Lazily,
            initialValue = AIDownloadProgress(AIDownloadState.NOT_DOWNLOADED, 0)
        )
    }

    private fun updateDownloadProgress(state: AIDownloadState, progress: Int): AIDownloadProgress {
        val currentTime = System.currentTimeMillis()
        
        when (state) {
            AIDownloadState.NOT_DOWNLOADED -> {
                _isDownloading.value = false
                _downloadProgress.value = AIDownloadProgress(
                    state = state,
                    progress = 0,
                    errorMessage = null
                )
            }
            
            AIDownloadState.DOWNLOADING -> {
                _isDownloading.value = true
                
                if (downloadStartTime == 0L) {
                    downloadStartTime = currentTime
                    lastProgressUpdate = currentTime
                }
                
                // Calculate download speed and time remaining
                val elapsedSeconds = (currentTime - downloadStartTime) / 1000.0
                val downloadSpeed = if (elapsedSeconds > 0) {
                    val speedKbps = (progress * 10) / elapsedSeconds // Rough estimate
                    "${speedKbps.toInt()} KB/s"
                } else {
                    "Calculating..."
                }
                
                val timeRemaining = if (progress > 0 && elapsedSeconds > 0) {
                    val remainingProgress = 100 - progress
                    val estimatedTotalTime = elapsedSeconds * 100 / progress
                    val remainingTime = estimatedTotalTime - elapsedSeconds
                    if (remainingTime > 60) {
                        "${(remainingTime / 60).toInt()} min ${(remainingTime % 60).toInt()} sec"
                    } else {
                        "${remainingTime.toInt()} sec"
                    }
                } else {
                    "Calculating..."
                }
                
                _downloadProgress.value = AIDownloadProgress(
                    state = state,
                    progress = progress,
                    downloadSpeed = downloadSpeed,
                    timeRemaining = timeRemaining,
                    errorMessage = null
                )
            }
            
            AIDownloadState.INSTALLING -> {
                _isDownloading.value = true
                _downloadProgress.value = AIDownloadProgress(
                    state = state,
                    progress = 100,
                    downloadSpeed = "",
                    timeRemaining = "Installing...",
                    errorMessage = null
                )
            }
            
            AIDownloadState.READY -> {
                _isDownloading.value = false
                downloadStartTime = 0L
                _downloadProgress.value = AIDownloadProgress(
                    state = state,
                    progress = 100,
                    downloadSpeed = "",
                    timeRemaining = "",
                    errorMessage = null
                )
            }
            
            AIDownloadState.FAILED -> {
                _isDownloading.value = false
                downloadStartTime = 0L
                _downloadProgress.value = AIDownloadProgress(
                    state = state,
                    progress = 0,
                    downloadSpeed = "",
                    timeRemaining = "",
                    errorMessage = getFailureReason(),
                    isRetryable = true
                )
            }
        }
        
        return _downloadProgress.value
    }

    fun startDownload(): Boolean {
        val manager = initializeAIFeatureManager()
        
        // Check device compatibility first
        if (!AIUtils.checkDeviceCompatibility(context)) {
            _downloadProgress.value = AIDownloadProgress(
                state = AIDownloadState.FAILED,
                progress = 0,
                errorMessage = "Device not compatible with AI features",
                isRetryable = false
            )
            return false
        }
        
        // Check if already available
        if (manager.isAIAvailable()) {
            _downloadProgress.value = AIDownloadProgress(
                state = AIDownloadState.READY,
                progress = 100
            )
            return true
        }
        
        try {
            downloadStartTime = System.currentTimeMillis()
            manager.downloadAIFeatures()
            return true
        } catch (e: Exception) {
            Log.e("AIDownloadManager", "Failed to start AI download", e)
            _downloadProgress.value = AIDownloadProgress(
                state = AIDownloadState.FAILED,
                progress = 0,
                errorMessage = "Failed to start download: ${e.localizedMessage}",
                isRetryable = true
            )
            return false
        }
    }

    fun cancelDownload() {
        aiFeatureManager?.cancelDownload()
        _isDownloading.value = false
        downloadStartTime = 0L
        _downloadProgress.value = AIDownloadProgress(
            state = AIDownloadState.NOT_DOWNLOADED,
            progress = 0
        )
    }

    fun retryDownload(): Boolean {
        Log.d("AIDownloadManager", "Retrying AI download")
        return startDownload()
    }

    fun uninstallAI() {
        aiFeatureManager?.uninstallAIFeatures()
        _downloadProgress.value = AIDownloadProgress(
            state = AIDownloadState.NOT_DOWNLOADED,
            progress = 0
        )
    }

    fun isAIAvailable(): Boolean {
        return aiFeatureManager?.isAIAvailable() ?: false
    }

    fun getAIModuleSize(): String {
        // Estimate AI module size (this would be dynamic in production)
        return "~15 MB"
    }

    fun getDownloadRequirements(): List<String> {
        return listOf(
            "Android ${AIUtils.MIN_SDK_VERSION}+ required",
            "At least ${AIUtils.MIN_RAM_MB}MB RAM",
            "Google Play Services required",
            "Stable internet connection recommended"
        )
    }

    private fun getFailureReason(): String {
        return when {
            !AIUtils.checkSDKVersion() -> "Device Android version too old"
            !AIUtils.checkRAMAvailability(context) -> "Insufficient RAM available"
            !AIUtils.checkPlayServices(context) -> "Google Play Services not available"
            else -> "Download failed. Please check your internet connection and try again."
        }
    }

    fun cleanup() {
        aiFeatureManager?.cleanup()
        aiFeatureManager = null
    }
}