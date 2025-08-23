package dev.sadakat.screentimetracker.ui.limiter.viewmodels

import android.app.Application // For app name resolution
import androidx.compose.runtime.State // Explicit import for State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.data.local.LimitedApp
import dev.sadakat.screentimetracker.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppViewItem( // For displaying in selection dialog
    val appName: String,
    val packageName: String
)

data class LimitedAppViewItem( // For displaying existing limits
    val appName: String, // Will need to resolve this
    val packageName: String,
    val timeLimitMillis: Long
)

data class LimiterConfigState(
    val isLoading: Boolean = false,
    val limitedApps: List<LimitedAppViewItem> = emptyList(),
    val installedAppsForSelection: List<InstalledAppViewItem> = emptyList(),
    val error: String? = null,
    val showAppSelectionDialog: Boolean = false,
    val selectedAppForLimit: InstalledAppViewItem? = null, // App for which limit is being set
    val appBeingEdited: LimitedAppViewItem? = null, // The app currently being edited
    val newLimitTimeInputMinutes: String = "10" // Default input for new limit
)

@HiltViewModel
class LimiterConfigViewModel @Inject constructor(
    private val getAllLimitedAppsUseCase: GetAllLimitedAppsUseCase,
    private val addLimitedAppUseCase: AddLimitedAppUseCase,
    private val removeLimitedAppUseCase: RemoveLimitedAppUseCase,
    private val updateLimitedAppUseCase: UpdateLimitedAppUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val application: Application // For app name resolution, Application is better than Context here
) : ViewModel() {

    private val _uiState = mutableStateOf(LimiterConfigState())
    val uiState: State<LimiterConfigState> = _uiState

    init {
        loadLimitedApps()
        loadInstalledAppsForSelection()
    }

    private fun loadLimitedApps() {
        viewModelScope.launch {
            getAllLimitedAppsUseCase().distinctUntilChanged().collect { apps ->
                val viewItems = apps.map { limitedApp ->
                    LimitedAppViewItem(
                        appName = getAppName(limitedApp.packageName),
                        packageName = limitedApp.packageName,
                        timeLimitMillis = limitedApp.timeLimitMillis
                    )
                }
                _uiState.value = _uiState.value.copy(limitedApps = viewItems.sortedBy { it.appName })
            }
        }
    }

    private fun loadInstalledAppsForSelection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val installed = getInstalledAppsUseCase().map { appInfo ->
                    InstalledAppViewItem(appName = getAppName(appInfo.packageName), packageName = appInfo.packageName)
                }
                // Filter out apps already limited from selection list
                val currentLimitedPackageNames = _uiState.value.limitedApps.map { it.packageName }.toSet()
                _uiState.value = _uiState.value.copy(
                    installedAppsForSelection = installed.filterNot { currentLimitedPackageNames.contains(it.packageName) },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load apps: ${e.message}")
            }
        }
    }

    fun onAddAppClicked() {
        // Refresh installed apps list, filtering out already limited ones
        loadInstalledAppsForSelection()
        _uiState.value = _uiState.value.copy(showAppSelectionDialog = true, selectedAppForLimit = null, appBeingEdited = null, newLimitTimeInputMinutes = "10", error = null)
    }

    fun onEditAppClicked(app: LimitedAppViewItem) {
        _uiState.value = _uiState.value.copy(
            showAppSelectionDialog = true,
            selectedAppForLimit = InstalledAppViewItem(app.appName, app.packageName),
            appBeingEdited = app,
            newLimitTimeInputMinutes = (app.timeLimitMillis / (60 * 1000)).toString(), // Convert millis to minutes
            error = null
        )
    }

    fun onDismissAppSelectionDialog() {
        _uiState.value = _uiState.value.copy(showAppSelectionDialog = false, selectedAppForLimit = null)
    }

    fun onAppSelectedForLimiting(app: InstalledAppViewItem) {
        _uiState.value = _uiState.value.copy(selectedAppForLimit = app)
        // Dialog will now show time input for this app
    }

    fun onNewLimitTimeChanged(timeMinutes: String) {
        _uiState.value = _uiState.value.copy(newLimitTimeInputMinutes = timeMinutes)
    }

    fun onConfirmAddLimitedApp() {
        val appToLimit = _uiState.value.selectedAppForLimit ?: return
        val timeMinutes = _uiState.value.newLimitTimeInputMinutes.toLongOrNull()

        if (timeMinutes == null || timeMinutes <= 0) {
            _uiState.value = _uiState.value.copy(error = "Limit must be a positive number of minutes.")
            return
        }

        viewModelScope.launch {
            try {
                val limitedApp = LimitedApp(appToLimit.packageName, timeMinutes * 60 * 1000)
                if (_uiState.value.appBeingEdited == null) {
                    addLimitedAppUseCase(limitedApp)
                } else {
                    updateLimitedAppUseCase(limitedApp)
                }
                _uiState.value = _uiState.value.copy(showAppSelectionDialog = false, selectedAppForLimit = null, appBeingEdited = null, error = null)
                loadLimitedApps() // Refresh the main list of limited apps
                loadInstalledAppsForSelection() // Refresh selection list after adding/updating
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(error = "Failed to add/update limit: ${e.message}")
            }
        }
    }

    fun onRemoveLimitedApp(packageName: String) {
        viewModelScope.launch {
            try {
                removeLimitedAppUseCase(LimitedApp(packageName, 0L)) // timeLimitMillis is not used by delete op
                loadLimitedApps() // Refresh the main list of limited apps
                loadInstalledAppsForSelection() // Refresh selection list after removing
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to remove limit: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = application.packageManager
            val applicationInfo = pm.getApplicationInfo(packageName, 0) // No need for GET_META_DATA here
            pm.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName // Fallback
        }
    }
}
