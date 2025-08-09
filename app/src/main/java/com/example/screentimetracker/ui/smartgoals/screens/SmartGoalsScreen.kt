package com.example.screentimetracker.ui.smartgoals.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screentimetracker.domain.usecases.SmartGoalSettingUseCase.GoalContext
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.smartgoals.components.GoalAdjustmentDialog
import com.example.screentimetracker.ui.smartgoals.components.GoalRecommendationCard
import com.example.screentimetracker.ui.smartgoals.viewmodels.SmartGoalsViewModel
import com.example.screentimetracker.ui.theme.*

@Composable
fun SmartGoalsScreen(
    modifier: Modifier = Modifier,
    viewModel: SmartGoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.generateAIRecommendations()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Header with refresh button
        PlayfulCard(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            gradientBackground = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "🤖 Smart Goal Recommendations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "AI-powered goals based on your usage patterns ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(
                    onClick = { viewModel.refreshRecommendations() },
                    enabled = !uiState.isLoadingRecommendations
                ) {
                    if (uiState.isLoadingRecommendations) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh recommendations",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Context filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedContext == null,
                    onClick = { viewModel.generateAIRecommendations() },
                    label = { Text("General") }
                )
            }
            items(GoalContext.entries) { context ->
                FilterChip(
                    selected = uiState.selectedContext == context,
                    onClick = { viewModel.setSelectedContext(context) },
                    label = { 
                        Text(
                            context.name.lowercase().split('_').joinToString(" ") { 
                                it.replaceFirstChar { char -> char.uppercase() } 
                            }
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Content
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoadingRecommendations) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                "🧠 Analyzing your habits...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else if (uiState.error != null) {
                item {
                    PlayfulCard(
                        backgroundColor = PlayfulAccent.copy(alpha = 0.1f),
                        gradientBackground = false
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "⚠️ Oops!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PlayfulAccent
                            )
                            Text(
                                uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            OutlinedButton(
                                onClick = { 
                                    viewModel.clearError()
                                    viewModel.refreshRecommendations() 
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Try Again")
                            }
                        }
                    }
                }
            } else if (uiState.recommendations.isEmpty()) {
                item {
                    PlayfulCard(
                        backgroundColor = SkyBlue.copy(alpha = 0.1f),
                        gradientBackground = true
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "🎯",
                                fontSize = 48.sp
                            )
                            Text(
                                "No recommendations yet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkyBlue
                            )
                            Text(
                                "Use your phone for a few days, then check back for personalized goal recommendations based on your usage patterns.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                            OutlinedButton(
                                onClick = { viewModel.refreshRecommendations() }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Check Again")
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "🎯 ${uiState.recommendations.size} Smart Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.recommendations) { recommendation ->
                    GoalRecommendationCard(
                        recommendation = recommendation,
                        onAccept = { viewModel.acceptRecommendation(recommendation) },
                        onReject = { viewModel.rejectRecommendation(recommendation) }
                    )
                }
                
                // Success message after creating goal
                if (uiState.createdGoalId != null) {
                    item {
                        PlayfulCard(
                            backgroundColor = LimeGreen.copy(alpha = 0.1f),
                            gradientBackground = true
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "🎉",
                                    fontSize = 32.sp
                                )
                                Column {
                                    Text(
                                        "Goal Created Successfully!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LimeGreen
                                    )
                                    Text(
                                        "Your new goal has been added and is now active. You can track your progress in the Goals section.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Goal adjustment dialog
    uiState.pendingAdjustment?.let { adjustment ->
        GoalAdjustmentDialog(
            adjustment = adjustment,
            isApplying = uiState.isApplyingAdjustment,
            onApply = { viewModel.applyGoalAdjustment(adjustment) },
            onDismiss = { viewModel.dismissAdjustment() }
        )
    }
}