package dev.sadakat.screentimetracker.ui.ai.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

data class AIInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: AIInsightType,
    val confidence: Float, // 0.0 to 1.0
    val actionable: Boolean = false,
    val action: AIInsightAction? = null,
    val severity: AIInsightSeverity = AIInsightSeverity.INFO
)

enum class AIInsightType {
    USAGE_PATTERN,
    WELLNESS_TREND,
    GOAL_PROGRESS,
    RECOMMENDATION,
    PREDICTION,
    WARNING
}

enum class AIInsightSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class AIInsightAction(
    val label: String,
    val action: () -> Unit
)

@Composable
fun AIInsightsCard(
    insights: List<AIInsight>,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = "AI Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh insights",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when {
                isLoading -> {
                    LoadingInsights()
                }
                insights.isEmpty() -> {
                    EmptyInsights()
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(insights) { insight ->
                            AIInsightItem(insight = insight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingInsights() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Generating AI insights...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyInsights() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Icon(
            Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No insights available yet",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Use the app for a few days to see AI-powered insights",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AIInsightItem(insight: AIInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (insight.severity) {
                AIInsightSeverity.INFO -> MaterialTheme.colorScheme.surfaceVariant
                AIInsightSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                AIInsightSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = getInsightIcon(insight.type),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when (insight.severity) {
                            AIInsightSeverity.INFO -> MaterialTheme.colorScheme.primary
                            AIInsightSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
                            AIInsightSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Confidence indicator
                ConfidenceBadge(confidence = insight.confidence)
            }
            
            // Action button if available
            insight.action?.let { action ->
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = action.action,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(action.label)
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val percentage = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.8f -> MaterialTheme.colorScheme.primary
        confidence >= 0.6f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        border = null
    ) {
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun getInsightIcon(type: AIInsightType): ImageVector {
    return when (type) {
        AIInsightType.USAGE_PATTERN -> Icons.Default.Timeline
        AIInsightType.WELLNESS_TREND -> Icons.Default.TrendingUp
        AIInsightType.GOAL_PROGRESS -> Icons.Default.TrackChanges
        AIInsightType.RECOMMENDATION -> Icons.Default.Lightbulb
        AIInsightType.PREDICTION -> Icons.Default.Psychology
        AIInsightType.WARNING -> Icons.Default.Warning
    }
}

// Sample data for demonstration
object SampleAIInsights {
    fun generateSampleInsights(): List<AIInsight> {
        return listOf(
            AIInsight(
                id = "1",
                title = "Evening Usage Spike",
                description = "You spend 40% more time on social media after 8 PM. Consider setting a wind-down routine.",
                type = AIInsightType.USAGE_PATTERN,
                confidence = 0.85f,
                actionable = true,
                action = AIInsightAction("Set Evening Goal") { 
                    // Navigate to goals screen or show goal creation dialog
                },
                severity = AIInsightSeverity.INFO
            ),
            AIInsight(
                id = "2",
                title = "Wellness Improvement",
                description = "Your digital wellness score increased by 12% this week! Keep up the good habits.",
                type = AIInsightType.WELLNESS_TREND,
                confidence = 0.92f,
                severity = AIInsightSeverity.INFO
            ),
            AIInsight(
                id = "3",
                title = "Goal Achievement Risk",
                description = "At your current pace, you may exceed your daily screen time goal by 45 minutes.",
                type = AIInsightType.PREDICTION,
                confidence = 0.78f,
                actionable = true,
                action = AIInsightAction("Take Break Now") { 
                    // Navigate to mindfulness/breathing exercise screen
                },
                severity = AIInsightSeverity.WARNING
            ),
            AIInsight(
                id = "4",
                title = "Focus Session Recommendation",
                description = "Based on your productivity patterns, 2-3 PM is your optimal focus time.",
                type = AIInsightType.RECOMMENDATION,
                confidence = 0.71f,
                actionable = true,
                action = AIInsightAction("Schedule Focus") { 
                    // Navigate to focus session creation screen
                },
                severity = AIInsightSeverity.INFO
            )
        )
    }
}