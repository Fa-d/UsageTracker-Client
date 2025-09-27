package dev.sadakat.screentimetracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Add repositories when available
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // TODO: Load actual settings from repositories
            _uiState.value = SettingsUiState(
                settingsSections = listOf(
                    SettingsSection(
                        title = "Notifications",
                        items = listOf(
                            SettingItem(
                                key = "break_reminders",
                                title = "Break Reminders",
                                subtitle = "Get notified when it's time for a break",
                                type = SettingType.SWITCH,
                                value = true
                            ),
                            SettingItem(
                                key = "usage_alerts",
                                title = "Usage Alerts",
                                subtitle = "Alert when daily limits are reached",
                                type = SettingType.SWITCH,
                                value = true
                            ),
                            SettingItem(
                                key = "wellness_reminders",
                                title = "Wellness Reminders",
                                subtitle = "Daily wellness check-ins",
                                type = SettingType.SWITCH,
                                value = false
                            ),
                            SettingItem(
                                key = "reminder_frequency",
                                title = "Reminder Frequency",
                                subtitle = "How often to show reminders",
                                type = SettingType.SLIDER,
                                value = 60f,
                                range = 15f to 180f,
                                unit = " min"
                            )
                        )
                    ),
                    SettingsSection(
                        title = "App Limits",
                        items = listOf(
                            SettingItem(
                                key = "daily_limit",
                                title = "Daily Screen Time Limit",
                                subtitle = "Maximum screen time per day",
                                type = SettingType.SLIDER,
                                value = 4f,
                                range = 1f to 12f,
                                unit = " hours"
                            ),
                            SettingItem(
                                key = "app_time_limits",
                                title = "Individual App Limits",
                                subtitle = "Set time limits for specific apps",
                                type = SettingType.ACTION,
                                value = null
                            ),
                            SettingItem(
                                key = "break_interval",
                                title = "Break Interval",
                                subtitle = "Time between suggested breaks",
                                type = SettingType.SLIDER,
                                value = 60f,
                                range = 30f to 120f,
                                unit = " min"
                            )
                        )
                    ),
                    SettingsSection(
                        title = "Privacy",
                        items = listOf(
                            SettingItem(
                                key = "data_collection",
                                title = "Usage Data Collection",
                                subtitle = "Allow anonymous usage analytics",
                                type = SettingType.SWITCH,
                                value = true
                            ),
                            SettingItem(
                                key = "crash_reports",
                                title = "Crash Reports",
                                subtitle = "Send crash reports to improve the app",
                                type = SettingType.SWITCH,
                                value = true
                            ),
                            SettingItem(
                                key = "data_export",
                                title = "Export Data",
                                subtitle = "Export your usage data",
                                type = SettingType.ACTION,
                                value = null
                            ),
                            SettingItem(
                                key = "clear_data",
                                title = "Clear All Data",
                                subtitle = "Remove all stored usage data",
                                type = SettingType.ACTION,
                                value = null
                            )
                        )
                    ),
                    SettingsSection(
                        title = "Appearance",
                        items = listOf(
                            SettingItem(
                                key = "theme",
                                title = "Theme",
                                subtitle = "Choose app appearance",
                                type = SettingType.DROPDOWN,
                                value = "System",
                                options = listOf("Light", "Dark", "System")
                            ),
                            SettingItem(
                                key = "color_scheme",
                                title = "Color Scheme",
                                subtitle = "Choose your preferred colors",
                                type = SettingType.DROPDOWN,
                                value = "Dynamic",
                                options = listOf("Dynamic", "Blue", "Green", "Purple")
                            )
                        )
                    ),
                    SettingsSection(
                        title = "About",
                        items = listOf(
                            SettingItem(
                                key = "version",
                                title = "Version",
                                subtitle = "",
                                type = SettingType.INFO,
                                value = "1.0.0"
                            ),
                            SettingItem(
                                key = "privacy_policy",
                                title = "Privacy Policy",
                                subtitle = "Read our privacy policy",
                                type = SettingType.ACTION,
                                value = null
                            ),
                            SettingItem(
                                key = "terms_of_service",
                                title = "Terms of Service",
                                subtitle = "Read our terms of service",
                                type = SettingType.ACTION,
                                value = null
                            ),
                            SettingItem(
                                key = "contact_support",
                                title = "Contact Support",
                                subtitle = "Get help or report issues",
                                type = SettingType.ACTION,
                                value = null
                            )
                        )
                    )
                )
            )
        }
    }

    fun updateSetting(key: String, value: Any) {
        val updatedSections = _uiState.value.settingsSections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    if (item.key == key) {
                        item.copy(value = value)
                    } else {
                        item
                    }
                }
            )
        }
        _uiState.value = _uiState.value.copy(settingsSections = updatedSections)

        // TODO: Persist setting changes to repository
        saveSettingChange(key, value)
    }

    fun onSettingClicked(key: String) {
        when (key) {
            "app_time_limits" -> navigateToAppLimits()
            "data_export" -> exportData()
            "clear_data" -> clearAllData()
            "privacy_policy" -> openPrivacyPolicy()
            "terms_of_service" -> openTermsOfService()
            "contact_support" -> contactSupport()
        }
    }

    private fun saveSettingChange(key: String, value: Any) {
        // TODO: Implement settings persistence
        viewModelScope.launch {
            // Save to preferences or repository
        }
    }

    private fun navigateToAppLimits() {
        // TODO: Navigate to app limits screen
    }

    private fun exportData() {
        // TODO: Implement data export functionality
    }

    private fun clearAllData() {
        // TODO: Implement data clearing with confirmation
    }

    private fun openPrivacyPolicy() {
        // TODO: Open privacy policy
    }

    private fun openTermsOfService() {
        // TODO: Open terms of service
    }

    private fun contactSupport() {
        // TODO: Open support contact options
    }
}

data class SettingsUiState(
    val settingsSections: List<SettingsSection> = emptyList(),
    val isLoading: Boolean = false
)

data class SettingsSection(
    val title: String,
    val items: List<SettingItem>
)

data class SettingItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val type: SettingType,
    val value: Any?,
    val range: Pair<Float, Float>? = null,
    val unit: String = "",
    val options: List<String>? = null
)

enum class SettingType {
    SWITCH,
    SLIDER,
    DROPDOWN,
    ACTION,
    INFO
}