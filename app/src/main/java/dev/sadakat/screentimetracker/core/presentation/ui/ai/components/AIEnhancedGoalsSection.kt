package dev.sadakat.screentimetracker.core.presentation.ui.ai.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.presentation.ui.smartgoals.viewmodels.SmartGoalsViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.theme.LimeGreen
import dev.sadakat.screentimetracker.core.presentation.ui.theme.SkyBlue
import dev.sadakat.screentimetracker.core.presentation.ui.theme.VibrantOrange

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AIEnhancedGoalsSection(
    modifier: Modifier = Modifier,
    isAIEnabled: Boolean = false,
    onEnableAI: () -> Unit = {},
    onNavigateToSmartGoals: (() -> Unit)? = null
) {
    val smartGoalsViewModel: SmartGoalsViewModel = hiltViewModel()
    val smartGoalsState by smartGoalsViewModel.uiState
    
    var showAIInsights by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Insights Section (if enabled)
        if (isAIEnabled) {
            AIInsightsSection(
                expanded = showAIInsights,
                onToggleExpanded = { showAIInsights = !showAIInsights },
                onViewDetails = { /* Navigate to AI insights */ }
            )
        }
        
        // Enhanced Smart Recommendations Section
        PlayfulCard(
            backgroundColor = if (isAIEnabled) {
                LimeGreen.copy(alpha = 0.15f)
            } else {
                SkyBlue.copy(alpha = 0.1f)
            },
            gradientBackground = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (isAIEnabled) "🧠 AI-Powered Goals" else "🤖 Smart Recommendations",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (isAIEnabled) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "AI Enhanced",
                                    tint = LimeGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Text(
                            if (isAIEnabled) {
                                "Personalized recommendations with success predictions"
                            } else {
                                "Enable AI for personalized insights"
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (isAIEnabled) {
                        TextButton(
                            onClick = { 
                                onNavigateToSmartGoals?.invoke() ?: smartGoalsViewModel.generateAIRecommendations() 
                            }
                        ) {
                            Text("View All", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        TextButton(onClick = onEnableAI) {
                            Text("Enable AI", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                if (isAIEnabled) {
                    AIEnabledGoalsContent(
                        smartGoalsState = smartGoalsState,
                        smartGoalsViewModel = smartGoalsViewModel
                    )
                } else {
                    AIDisabledGoalsContent(onEnableAI = onEnableAI)
                }
            }
        }
    }
}

@Composable
private fun AIInsightsSection(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onViewDetails: () -> Unit
) {
    PlayfulCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        gradientBackground = true
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "AI Insights",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Column {
                        Text(
                            "AI Insights",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Personalized usage analysis",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                AIInsightsPreview(onViewDetails = onViewDetails)
            }
        }
    }
}

@Composable
private fun AIInsightsPreview(
    onViewDetails: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Mock insights data - in real implementation, this would come from AI analysis
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Tomorrow's Prediction",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    "3h 45m",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color(0xFF27AE60), RoundedCornerShape(3.dp)
                            )
                    )
                    Text(
                        "Low Risk",
                        fontSize = 10.sp,
                        color = Color(0xFF27AE60)
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "Top Insight",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    "Morning Focus",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.End
                )
                Text(
                    "Peak productivity time detected",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    textAlign = TextAlign.End
                )
            }
        }
        
        TextButton(
            onClick = onViewDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "View Detailed Analysis",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AIEnabledGoalsContent(
    smartGoalsState: Any, // Replace with actual state type
    smartGoalsViewModel: SmartGoalsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // AI-Enhanced Goal Recommendations
        AIGoalRecommendationsPreview()
        
        // Success Probability Indicators
        AISuccessPredictions()
        
        // Quick AI Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { /* Generate new recommendations */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", fontSize = 12.sp)
            }
            
            OutlinedButton(
                onClick = { /* Optimize current goals */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Optimize", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AIDisabledGoalsContent(
    onEnableAI: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Text(
            "Unlock AI-Powered Goal Intelligence",
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            "Get personalized goal recommendations, success predictions, and adaptive strategies based on your unique usage patterns.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Button(
            onClick = onEnableAI,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enable AI Features")
        }
    }
}

@Composable
private fun AIGoalRecommendationsPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AI Recommendation",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = LimeGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "85% success rate",
                        fontSize = 11.sp,
                        color = LimeGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Text(
                "Reduce daily screen time to 3.5 hours",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "Based on your morning usage patterns, this goal aligns with your natural rhythms and has a high probability of success.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Reject recommendation */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pass", fontSize = 11.sp)
                }
                
                Button(
                    onClick = { /* Accept recommendation */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AISuccessPredictions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Goal Success Predictions",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            
            // Mock data for current goals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Daily Screen Time",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "78%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LimeGreen
                    )
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = LimeGreen,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Unlock Frequency",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "65%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = VibrantOrange
                    )
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = VibrantOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}