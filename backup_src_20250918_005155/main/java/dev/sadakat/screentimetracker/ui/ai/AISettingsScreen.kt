package dev.sadakat.screentimetracker.ui.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sadakat.screentimetracker.ui.ai.viewmodels.AIViewModel
import dev.sadakat.screentimetracker.ui.ai.viewmodels.AIFeatureState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Features") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AIStatusCard(
                    uiState = uiState,
                    onEnableAI = viewModel::enableAIFeatures,
                    onDisableAI = viewModel::disableAIFeatures,
                    onDownloadAI = viewModel::startAIDownload
                )
            }
            
            if (uiState.isAvailable) {
                item {
                    AIFeatureToggles(
                        uiState = uiState,
                        onUpdateInsights = viewModel::updateAIInsightsEnabled,
                        onUpdateGoalRecommendations = viewModel::updateAIGoalRecommendationsEnabled,
                        onUpdatePredictiveCoaching = viewModel::updateAIPredictiveCoachingEnabled,
                        onUpdateUsagePredictions = viewModel::updateAIUsagePredictionsEnabled
                    )
                }
            }
            
            item {
                AIInfoCard(
                    moduleSize = viewModel.getAIModuleSize(),
                    requirements = viewModel.getDownloadRequirements()
                )
            }
        }
    }
}

@Composable
private fun AIStatusCard(
    uiState: AIFeatureState,
    onEnableAI: () -> Unit,
    onDisableAI: () -> Unit,
    onDownloadAI: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Features Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                when {
                    uiState.isDownloading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    uiState.isAvailable -> {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Available",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Not Available",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    uiState.isDownloading -> "Downloading AI features..."
                    uiState.isAvailable -> "AI features are ready to use"
                    uiState.availabilityStatus?.isDeviceCompatible == false -> "Device not compatible"
                    else -> "AI features not downloaded"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (uiState.downloadProgress?.progress != null && uiState.isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uiState.downloadProgress.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${uiState.downloadProgress.progress}% • ${uiState.downloadProgress.timeRemaining}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                uiState.isDownloading -> {
                    // Show download progress, handled above
                }
                uiState.isAvailable -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = if (uiState.isEnabled) onDisableAI else onEnableAI,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (uiState.isEnabled) "Disable" else "Enable")
                        }
                    }
                }
                uiState.availabilityStatus?.isDeviceCompatible == true -> {
                    Button(
                        onClick = onDownloadAI,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download AI Features")
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Device Not Compatible")
                    }
                }
            }
        }
    }
}

@Composable
private fun AIFeatureToggles(
    uiState: AIFeatureState,
    onUpdateInsights: (Boolean) -> Unit,
    onUpdateGoalRecommendations: (Boolean) -> Unit,
    onUpdatePredictiveCoaching: (Boolean) -> Unit,
    onUpdateUsagePredictions: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "AI Feature Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val prefs = uiState.userPreferences
            
            AIFeatureToggle(
                title = "Smart Insights",
                description = "Get personalized insights about your digital habits",
                checked = prefs?.aiInsightsEnabled ?: false,
                onCheckedChange = onUpdateInsights,
                enabled = uiState.isEnabled
            )
            
            AIFeatureToggle(
                title = "Goal Recommendations",
                description = "Receive AI-powered suggestions for wellness goals",
                checked = prefs?.aiGoalRecommendationsEnabled ?: false,
                onCheckedChange = onUpdateGoalRecommendations,
                enabled = uiState.isEnabled
            )
            
            AIFeatureToggle(
                title = "Predictive Coaching",
                description = "Get proactive coaching based on usage patterns",
                checked = prefs?.aiPredictiveCoachingEnabled ?: false,
                onCheckedChange = onUpdatePredictiveCoaching,
                enabled = uiState.isEnabled
            )
            
            AIFeatureToggle(
                title = "Usage Predictions",
                description = "See predictions for your future app usage",
                checked = prefs?.aiUsagePredictionsEnabled ?: false,
                onCheckedChange = onUpdateUsagePredictions,
                enabled = uiState.isEnabled
            )
        }
    }
}

@Composable
private fun AIFeatureToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun AIInfoCard(
    moduleSize: String,
    requirements: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Download Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Module Size: $moduleSize",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Requirements:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            requirements.forEach { requirement ->
                Text(
                    text = "• $requirement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}