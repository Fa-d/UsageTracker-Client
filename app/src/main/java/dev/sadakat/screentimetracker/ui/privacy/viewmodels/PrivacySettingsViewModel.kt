package dev.sadakat.screentimetracker.ui.privacy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.data.local.entities.PrivacySettings
import dev.sadakat.screentimetracker.domain.usecases.DataExportUseCase
import dev.sadakat.screentimetracker.domain.usecases.PrivacyManagerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class PrivacySettingsUiState(
    val privacySettings: PrivacySettings = PrivacySettings(),
    val isGuestModeActive: Boolean = false,
    val guestModeEndTime: String = "",
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val lastExportTimeFormatted: String = ""
)

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val privacyManagerUseCase: PrivacyManagerUseCase,
    private val dataExportUseCase: DataExportUseCase
) : ViewModel() {
    
    private val _isExporting = MutableStateFlow(false)
    private val _exportMessage = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<PrivacySettingsUiState> = combine(
        privacyManagerUseCase.getPrivacySettings(),
        _isExporting,
        _exportMessage
    ) { privacySettings, isExporting, exportMessage ->
        PrivacySettingsUiState(
            privacySettings = privacySettings,
            isGuestModeActive = isGuestModeActiveSync(),
            guestModeEndTime = formatGuestModeEndTime(privacySettings),
            isExporting = isExporting,
            exportMessage = exportMessage,
            lastExportTimeFormatted = formatLastExportTime(privacySettings.lastDataExportTime)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrivacySettingsUiState()
    )
    
    fun enableStealthMode(password: String) {
        viewModelScope.launch {
            privacyManagerUseCase.enableStealthMode(password)
        }
    }
    
    fun disableStealthMode() {
        viewModelScope.launch {
            privacyManagerUseCase.disableStealthMode()
        }
    }
    
    fun enableGuestMode(durationMinutes: Int) {
        viewModelScope.launch {
            privacyManagerUseCase.enableGuestMode(durationMinutes)
        }
    }
    
    fun disableGuestMode() {
        viewModelScope.launch {
            privacyManagerUseCase.disableGuestMode()
        }
    }
    
    fun exportDataAsJson() {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val result = dataExportUseCase.exportDataAsJson()
                if (result.isSuccess) {
                    _exportMessage.value = "Data exported successfully to ${result.getOrNull()?.name}"
                } else {
                    _exportMessage.value = "Export failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }
    
    fun exportDataAsCsv() {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val result = dataExportUseCase.exportDataAsCsv()
                if (result.isSuccess) {
                    val fileCount = result.getOrNull()?.size ?: 0
                    _exportMessage.value = "Data exported successfully as $fileCount CSV files"
                } else {
                    _exportMessage.value = "Export failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }
    
    fun clearExportMessage() {
        _exportMessage.value = null
    }
    
    private suspend fun isGuestModeActiveSync(): Boolean {
        return privacyManagerUseCase.isGuestModeActive()
    }
    
    private fun formatGuestModeEndTime(privacySettings: PrivacySettings): String {
        return if (privacySettings.isGuestModeEnabled && privacySettings.guestModeEndTime > 0) {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            dateFormat.format(Date(privacySettings.guestModeEndTime))
        } else {
            ""
        }
    }
    
    private fun formatLastExportTime(timestamp: Long): String {
        return if (timestamp > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        } else {
            ""
        }
    }
}