package dev.sadakat.screentimetracker.shared.presentation.settings

import kotlinx.serialization.Serializable

@Serializable
data class SettingsUiState(
    val settingsSections: List<SettingsSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Serializable
data class SettingsSection(
    val title: String,
    val items: List<SettingItem>
)

@Serializable
data class SettingItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val type: SettingType,
    val value: String? = null,
    val booleanValue: Boolean? = null,
    val floatValue: Float? = null,
    val range: Pair<Float, Float>? = null,
    val unit: String = "",
    val options: List<String>? = null
)

@Serializable
enum class SettingType {
    SWITCH,
    SLIDER,
    DROPDOWN,
    ACTION,
    INFO
}