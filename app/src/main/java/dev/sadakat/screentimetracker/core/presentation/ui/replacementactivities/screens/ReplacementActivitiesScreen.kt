package dev.sadakat.screentimetracker.core.presentation.ui.replacementactivities.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sadakat.screentimetracker.core.presentation.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.presentation.ui.replacementactivities.viewmodels.ReplacementActivitiesViewModel
import dev.sadakat.screentimetracker.core.data.local.entities.ReplacementActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplacementActivitiesScreen(
    onBackPressed: () -> Unit,
    triggeredByAppBlock: Boolean = false,
    blockedAppName: String = "",
    onCreateCustomActivity: () -> Unit = {},
    viewModel: ReplacementActivitiesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("all") }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<ReplacementActivity?>(null) }
    
    LaunchedEffect(triggeredByAppBlock, blockedAppName) {
        if (triggeredByAppBlock) {
            viewModel.loadSuggestionsForBlockedApp(blockedAppName)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Healthy Alternatives") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onCreateCustomActivity
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add custom activity")
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
            // App block notification
            if (triggeredByAppBlock && blockedAppName.isNotEmpty()) {
                item {
                    AppBlockNotificationCard(
                        blockedAppName = blockedAppName,
                        suggestions = uiState.smartSuggestions
                    )
                }
            }
            
            // Quick stats
            if (!triggeredByAppBlock) {
                item {
                    ActivityStatsCard(
                        todayCount = uiState.todayCompletions,
                        weeklyStats = uiState.weeklyStats
                    )
                }
            }
            
            // Category filter
            item {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        viewModel.filterActivitiesByCategory(category)
                    }
                )
            }
            
            // Smart suggestions (if not triggered by app block)
            if (!triggeredByAppBlock && uiState.smartSuggestions.isNotEmpty()) {
                item {
                    SmartSuggestionsCard(
                        suggestions = uiState.smartSuggestions,
                        onActivitySelected = { activity ->
                            selectedActivity = activity
                            showCompletionDialog = true
                        }
                    )
                }
            }
            
            // All activities
            item {
                Text(
                    text = if (triggeredByAppBlock) "More Options" else "All Activities",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(uiState.filteredActivities) { activity ->
                ActivityCard(
                    activity = activity,
                    onActivitySelected = {
                        selectedActivity = activity
                        showCompletionDialog = true
                    }
                )
            }
        }
    }
    
    // Activity completion dialog
    if (showCompletionDialog && selectedActivity != null) {
        ActivityCompletionDialog(
            activity = selectedActivity!!,
            onDismiss = { 
                showCompletionDialog = false
                selectedActivity = null
            },
            onActivityCompleted = { rating, actualDuration, notes ->
                viewModel.completeActivity(
                    activityId = selectedActivity!!.id,
                    rating = rating,
                    actualDurationMinutes = actualDuration,
                    notes = notes,
                    contextTrigger = if (triggeredByAppBlock) "blocked_$blockedAppName" else "manual"
                )
                showCompletionDialog = false
                selectedActivity = null
            }
        )
    }
}

@Composable
private fun AppBlockNotificationCard(
    blockedAppName: String,
    suggestions: List<ReplacementActivity>
) {
    PlayfulCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$blockedAppName was blocked",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Try a healthy alternative instead!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (suggestions.isNotEmpty()) {
                Text(
                    text = "🎯 Suggested for you:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions.take(3)) { activity ->
                        SuggestionChip(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityStatsCard(
    todayCount: Int,
    weeklyStats: dev.sadakat.screentimetracker.core.domain.usecases.ActivityStats
) {
    PlayfulCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = todayCount.toString(),
                label = "Today",
                emoji = "✅"
            )
            StatItem(
                value = weeklyStats.totalCompletions.toString(),
                label = "This Week",
                emoji = "📊"
            )
            StatItem(
                value = "${(weeklyStats.averageRating * 20).toInt()}%", // Convert 5-star to percentage
                label = "Satisfaction",
                emoji = "😊"
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        Triple("all", "All", "🌟"),
        Triple("wellness", "Wellness", "🧘"),
        Triple("physical", "Physical", "💪"),
        Triple("mental", "Mental", "🧠"),
        Triple("productivity", "Productive", "✅")
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (category, label, emoji) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text("$emoji $label")
                }
            )
        }
    }
}

@Composable
private fun SmartSuggestionsCard(
    suggestions: List<ReplacementActivity>,
    onActivitySelected: (ReplacementActivity) -> Unit
) {
    PlayfulCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Smart Suggestions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(suggestions) { activity ->
                    SuggestionCard(
                        activity = activity,
                        onClick = { onActivitySelected(activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    activity: ReplacementActivity,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "press_animation"
    )
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = getCategoryColor(activity.category).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = activity.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Text(
                text = "${activity.estimatedDurationMinutes}m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun SuggestionChip(activity: ReplacementActivity) {
    AssistChip(
        onClick = { /* Handle click */ },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(activity.emoji)
                Spacer(modifier = Modifier.width(4.dp))
                Text(activity.title, maxLines = 1)
            }
        }
    )
}

@Composable
private fun ActivityCard(
    activity: ReplacementActivity,
    onActivitySelected: () -> Unit
) {
    PlayfulCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActivitySelected() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                getCategoryColor(activity.category).copy(alpha = 0.2f),
                                getCategoryColor(activity.category).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Activity details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip(
                        text = "${activity.estimatedDurationMinutes}m",
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Chip(
                        text = getDifficultyText(activity.difficultyLevel),
                        color = getDifficultyColor(activity.difficultyLevel)
                    )
                    
                    if (activity.averageRating > 0) {
                        Chip(
                            text = "⭐ ${String.format("%.1f", activity.averageRating)}",
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Start activity",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Chip(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ActivityCompletionDialog(
    activity: ReplacementActivity,
    onDismiss: () -> Unit,
    onActivityCompleted: (rating: Int, actualDuration: Int, notes: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var actualDuration by remember { mutableStateOf(activity.estimatedDurationMinutes) }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(activity.emoji)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Activity")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("How was your ${activity.title.lowercase()}?")
                
                // Star rating
                Text("Rate your experience:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 }
                        ) {
                            Icon(
                                if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                // Duration slider
                Text("Actual duration: ${actualDuration}m", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = actualDuration.toFloat(),
                    onValueChange = { actualDuration = it.toInt() },
                    valueRange = 1f..30f,
                    steps = 28
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("How do you feel?") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onActivityCompleted(rating, actualDuration, notes) },
                enabled = rating > 0
            ) {
                Text("Complete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "wellness" -> Color(0xFF4CAF50)
        "physical" -> Color(0xFFFF9800)
        "mental" -> Color(0xFF2196F3)
        "productivity" -> Color(0xFF9C27B0)
        else -> Color(0xFF607D8B)
    }
}

private fun getDifficultyText(difficulty: Int): String {
    return when (difficulty) {
        1 -> "Easy"
        2 -> "Medium"
        3 -> "Hard"
        else -> "Easy"
    }
}

private fun getDifficultyColor(difficulty: Int): Color {
    return when (difficulty) {
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFFFF9800)
        3 -> Color(0xFFF44336)
        else -> Color(0xFF4CAF50)
    }
}