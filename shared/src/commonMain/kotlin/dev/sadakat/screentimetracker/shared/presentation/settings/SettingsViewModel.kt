package dev.sadakat.screentimetracker.shared.presentation.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val coroutineScope: CoroutineScope
) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // TODO: Load actual settings from repositories
                val settingsSections = listOf(
                    SettingsSection(
                        title = "Notifications",
                        items = listOf(
                            SettingItem(
                                key = "break_reminders",
                                title = "Break Reminders",
                                subtitle = "Get notified when it's time for a break",
                                type = SettingType.SWITCH,
                                booleanValue = true
                            ),
                            SettingItem(
                                key = "usage_alerts",
                                title = "Usage Alerts",
                                subtitle = "Alert when daily limits are reached",
                                type = SettingType.SWITCH,
                                booleanValue = true
                            ),
                            SettingItem(
                                key = "wellness_reminders",
                                title = "Wellness Reminders",
                                subtitle = "Daily wellness check-ins",
                                type = SettingType.SWITCH,
                                booleanValue = false
                            ),
                            SettingItem(
                                key = "reminder_frequency",
                                title = "Reminder Frequency",
                                subtitle = "How often to show reminders",
                                type = SettingType.SLIDER,
                                floatValue = 60f,
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
                                floatValue = 4f,
                                range = 1f to 12f,
                                unit = " hours"
                            ),
                            SettingItem(
                                key = "app_time_limits",
                                title = "Individual App Limits",
                                subtitle = "Set time limits for specific apps",
                                type = SettingType.ACTION
                            ),
                            SettingItem(
                                key = "break_interval",
                                title = "Break Interval",
                                subtitle = "Time between suggested breaks",
                                type = SettingType.SLIDER,
                                floatValue = 60f,
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
                                booleanValue = true
                            ),
                            SettingItem(
                                key = "crash_reports",
                                title = "Crash Reports",
                                subtitle = "Send crash reports to improve the app",
                                type = SettingType.SWITCH,
                                booleanValue = true
                            ),
                            SettingItem(
                                key = "data_export",
                                title = "Export Data",
                                subtitle = "Export your usage data",
                                type = SettingType.ACTION
                            ),
                            SettingItem(
                                key = "clear_data",
                                title = "Clear All Data",
                                subtitle = "Remove all stored usage data",
                                type = SettingType.ACTION
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
                                type = SettingType.ACTION
                            ),
                            SettingItem(
                                key = "terms_of_service",
                                title = "Terms of Service",
                                subtitle = "Read our terms of service",
                                type = SettingType.ACTION
                            ),
                            SettingItem(
                                key = "contact_support",
                                title = "Contact Support",
                                subtitle = "Get help or report issues",
                                type = SettingType.ACTION
                            )
                        )
                    )
                )

                _uiState.value = _uiState.value.copy(
                    settingsSections = settingsSections,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateSetting(key: String, value: Any) {
        val updatedSections = _uiState.value.settingsSections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    if (item.key == key) {
                        when (value) {
                            is Boolean -> item.copy(booleanValue = value)
                            is Float -> item.copy(floatValue = value)
                            is String -> item.copy(value = value)
                            else -> item
                        }
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
        coroutineScope.launch {
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

    fun refresh() {
        loadSettings()
    }
}