package dev.sadakat.screentimetracker.feature.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sadakat.screentimetracker.core.ui.components.*
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        ScreenContainer(
            title = "Goals",
            modifier = Modifier
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = viewModel::showCreateGoalDialog
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(CoreSpacing.minorSpacing))
                        Text("New Goal")
                    }
                }
            }

            item {
                GoalsOverviewCard(
                    totalGoals = uiState.goals.size,
                    completedGoals = uiState.goals.count { it.isCompleted },
                    activeGoals = uiState.goals.count { !it.isCompleted },
                    weeklyProgress = uiState.weeklyProgress
                )
            }

            item {
                TabSelector(
                    items = GoalCategory.values().map {
                        SelectorItem(it, it.displayName)
                    },
                    selectedItem = uiState.selectedCategory,
                    onItemSelected = viewModel::selectCategory
                )
            }

            items(uiState.filteredGoals) { goal ->
                GoalCard(
                    goal = goal,
                    onToggleComplete = { viewModel.toggleGoalCompletion(goal.id) },
                    onEdit = { viewModel.editGoal(goal.id) },
                    onDelete = { viewModel.deleteGoal(goal.id) }
                )
            }

            if (uiState.filteredGoals.isEmpty()) {
                item {
                    EmptyGoalsState(
                        category = uiState.selectedCategory,
                        onCreateGoal = viewModel::showCreateGoalDialog
                    )
                }
            }
        }

        if (uiState.showCreateGoalDialog) {
            CreateGoalDialog(
                onDismiss = viewModel::hideCreateGoalDialog,
                onCreateGoal = viewModel::createGoal
            )
        }
    }
}

@Composable
private fun GoalsOverviewCard(
    totalGoals: Int,
    completedGoals: Int,
    activeGoals: Int,
    weeklyProgress: Float,
    modifier: Modifier = Modifier
) {
    MetricCard(
        title = "Goals Overview",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Total",
                value = totalGoals.toString(),
                valueColor = MaterialTheme.colorScheme.primary
            )
            MetricItem(
                label = "Completed",
                value = completedGoals.toString(),
                valueColor = MaterialTheme.colorScheme.secondary
            )
            MetricItem(
                label = "Active",
                value = activeGoals.toString(),
                valueColor = MaterialTheme.colorScheme.tertiary
            )
        }

        ProgressCard(
            title = "",
            progress = weeklyProgress,
            label = "Weekly Progress",
            value = "${(weeklyProgress * 100).toInt()}%"
        )
    }
}

@Composable
private fun GoalsStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GoalCategoryTabs(
    selectedCategory: GoalCategory,
    onCategorySelected: (GoalCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedCategory.ordinal,
        modifier = modifier
    ) {
        GoalCategory.values().forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(category.displayName) }
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (goal.description.isNotEmpty()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit goal")
                    }
                    Checkbox(
                        checked = goal.isCompleted,
                        onCheckedChange = { onToggleComplete() }
                    )
                }
            }

            if (goal.targetValue != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${goal.currentValue}/${goal.targetValue} ${goal.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { goal.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(goal.category.displayName) }
                )

                Text(
                    text = "Due: ${goal.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyGoalsState(
    category: GoalCategory,
    onCreateGoal: () -> Unit,
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier
) {
    EmptyStateCard(
        emoji = "🎯",
        title = if (category == GoalCategory.ALL) {
            "No goals yet"
        } else {
            "No ${category.displayName.lowercase()} goals"
        },
        description = "Create your first goal to start tracking your digital wellness journey",
        actionText = "Create Goal",
        onActionClick = onCreateGoal
    )
}

@Composable
private fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onCreateGoal: (String, String, GoalCategory) -> Unit,
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.SCREEN_TIME) }

    FormDialog(
        title = "Create New Goal",
        onDismiss = onDismiss,
        onConfirm = {
            if (title.isNotBlank()) {
                onCreateGoal(title, description, selectedCategory)
            }
        },
        confirmText = "Create",
        confirmEnabled = title.isNotBlank()
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Goal Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}