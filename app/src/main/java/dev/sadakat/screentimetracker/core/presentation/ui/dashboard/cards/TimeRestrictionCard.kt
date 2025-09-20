package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.core.presentation.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.presentation.ui.theme.ColorfulPrimary
import dev.sadakat.screentimetracker.core.presentation.ui.theme.ColorfulSecondary
import dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.viewmodels.RestrictionStatusPreview
import dev.sadakat.screentimetracker.core.data.local.entities.TimeRestriction

@Composable
fun TimeRestrictionCard(
    activeRestrictions: List<TimeRestriction>,
    allRestrictions: List<TimeRestriction>,
    onToggleRestriction: (TimeRestriction) -> Unit,
    onNavigateToSettings: () -> Unit,
    formatTime: (Int) -> String,
    formatTimeUntil: (Int) -> String,
    getRestrictionStatusPreview: (TimeRestriction) -> RestrictionStatusPreview
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    PlayfulCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (activeRestrictions.isNotEmpty()) Icons.Default.Block else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (activeRestrictions.isNotEmpty()) Color.Red else ColorfulSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Time Restrictions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (activeRestrictions.isNotEmpty()) {
                                "${activeRestrictions.size} active"
                            } else {
                                "${allRestrictions.count { it.isEnabled }} enabled"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    if (isExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Restrictions Status
            if (activeRestrictions.isNotEmpty()) {
                ActiveRestrictionsSection(
                    activeRestrictions = activeRestrictions,
                    formatTime = formatTime,
                    formatTimeUntil = formatTimeUntil,
                    getRestrictionStatusPreview = getRestrictionStatusPreview
                )
            } else {
                NoActiveRestrictionsSection(
                    enabledCount = allRestrictions.count { it.isEnabled },
                    nextRestriction = getNextUpcomingRestriction(allRestrictions, getRestrictionStatusPreview),
                    formatTime = formatTime,
                    formatTimeUntil = formatTimeUntil,
                    getRestrictionStatusPreview = getRestrictionStatusPreview
                )
            }

            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (allRestrictions.isEmpty()) {
                        EmptyRestrictionsSection(onNavigateToSettings)
                    } else {
                        RestrictionsManagementSection(
                            restrictions = allRestrictions,
                            onToggle = onToggleRestriction,
                            onNavigateToSettings = onNavigateToSettings,
                            formatTime = formatTime,
                            getRestrictionStatusPreview = getRestrictionStatusPreview
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveRestrictionsSection(
    activeRestrictions: List<TimeRestriction>,
    formatTime: (Int) -> String,
    formatTimeUntil: (Int) -> String,
    getRestrictionStatusPreview: (TimeRestriction) -> RestrictionStatusPreview
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                "🚫 Currently Blocking Apps",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeRestrictions.take(3)) { restriction ->
                    val statusPreview = getRestrictionStatusPreview(restriction)
                    RestrictionChip(
                        name = restriction.name,
                        timeUntil = statusPreview.timeUntilChange?.let { formatTimeUntil(it) },
                        isActive = true
                    )
                }
                
                if (activeRestrictions.size > 3) {
                    item {
                        RestrictionChip(
                            name = "+${activeRestrictions.size - 3} more",
                            timeUntil = null,
                            isActive = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoActiveRestrictionsSection(
    enabledCount: Int,
    nextRestriction: Pair<TimeRestriction, RestrictionStatusPreview>?,
    formatTime: (Int) -> String,
    formatTimeUntil: (Int) -> String,
    getRestrictionStatusPreview: (TimeRestriction) -> RestrictionStatusPreview
) {
    if (enabledCount > 0) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ColorfulSecondary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "✅ No Active Restrictions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Green
                )
                
                nextRestriction?.let { (restriction, preview) ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Next: ${restriction.name} in ${preview.timeUntilChange?.let { formatTimeUntil(it) } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Text(
            "No restrictions enabled. All apps are accessible.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyRestrictionsSection(onNavigateToSettings: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "No time restrictions set up",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onNavigateToSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorfulPrimary
            )
        ) {
            Text("Set Up Restrictions")
        }
    }
}

@Composable
private fun RestrictionsManagementSection(
    restrictions: List<TimeRestriction>,
    onToggle: (TimeRestriction) -> Unit,
    onNavigateToSettings: () -> Unit,
    formatTime: (Int) -> String,
    getRestrictionStatusPreview: (TimeRestriction) -> RestrictionStatusPreview
) {
    Column {
        Text(
            "Quick Controls",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        restrictions.take(3).forEach { restriction ->
            val statusPreview = getRestrictionStatusPreview(restriction)
            RestrictionToggleRow(
                restriction = restriction,
                statusPreview = statusPreview,
                onToggle = { onToggle(restriction) },
                formatTime = formatTime
            )
        }
        
        if (restrictions.size > 3) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "+${restrictions.size - 3} more restrictions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage All Restrictions")
        }
    }
}

@Composable
private fun RestrictionToggleRow(
    restriction: TimeRestriction,
    statusPreview: RestrictionStatusPreview,
    onToggle: () -> Unit,
    formatTime: (Int) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    restriction.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (statusPreview.isCurrentlyActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.Green.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Text(
                "${formatTime(restriction.startTimeMinutes)} - ${formatTime(restriction.endTimeMinutes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = restriction.isEnabled,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun RestrictionChip(
    name: String,
    timeUntil: String?,
    isActive: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color.Red.copy(alpha = 0.2f) else ColorfulSecondary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isActive) Color.Red else ColorfulSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            timeUntil?.let { time ->
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Color.Red.copy(alpha = 0.7f) else ColorfulSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getNextUpcomingRestriction(
    restrictions: List<TimeRestriction>,
    getStatusPreview: (TimeRestriction) -> RestrictionStatusPreview
): Pair<TimeRestriction, RestrictionStatusPreview>? {
    return restrictions
        .filter { it.isEnabled }
        .mapNotNull { restriction ->
            val preview = getStatusPreview(restriction)
            if (!preview.isCurrentlyActive && preview.timeUntilChange != null) {
                restriction to preview
            } else null
        }
        .minByOrNull { (_, preview) -> preview.timeUntilChange ?: Int.MAX_VALUE }
}