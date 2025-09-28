import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.sadakat.screentimetracker.shared.di.commonModule
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardScreen
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(commonModule)
    }

    val windowState = rememberWindowState()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Screen Time Tracker",
        state = windowState
    ) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DesktopApp()
            }
        }
    }
}

@Composable
fun DesktopApp() {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    // For now, show a mock dashboard
    val mockUiState = DashboardUiState(
        totalScreenTimeFormatted = "2h 45m",
        pickupsToday = 12,
        wellnessScore = 0.85f,
        topApps = emptyList()
    )

    DashboardScreen(
        uiState = mockUiState,
        onTakeBreak = { /* TODO */ },
        onNavigateToGoals = { /* TODO */ },
        onNavigateToWellness = { /* TODO */ }
    )
}