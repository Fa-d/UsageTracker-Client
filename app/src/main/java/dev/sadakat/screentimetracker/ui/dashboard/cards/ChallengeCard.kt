package dev.sadakat.screentimetracker.ui.dashboard.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.data.local.entities.Challenge
import dev.sadakat.screentimetracker.domain.usecases.ChallengeManagerUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeCard(
    challengeManager: ChallengeManagerUseCase,
    modifier: Modifier = Modifier
) {
    val challenges by challengeManager.getActiveChallenges().collectAsState(initial = emptyList())

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 Weekly Challenges",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (challenges.isNotEmpty()) {
                    val completedCount = challenges.count { it.status == "completed" }
                    Text(
                        text = "$completedCount/${challenges.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (challenges.isEmpty()) {
                Text(
                    text = "No active challenges this week. Check back soon!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(challenges) { challenge ->
                        ChallengeItem(challenge = challenge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeItem(
    challenge: Challenge,
    modifier: Modifier = Modifier
) {
    val progress = if (challenge.targetValue > 0) {
        (challenge.currentProgress.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val statusColor = when (challenge.status) {
        "completed" -> Color(0xFF4CAF50) // Green
        "active" -> MaterialTheme.colorScheme.primary
        "failed" -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }

    val backgroundColor = when (challenge.status) {
        "completed" -> Color(0xFFE8F5E8)
        "active" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        "failed" -> Color(0xFFFFEBEE)
        else -> Color.LightGray
    }

    Card(
        modifier = modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = challenge.emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = challenge.name,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(statusColor)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${challenge.currentProgress}/${challenge.targetValue}",
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )
            
            if (challenge.status == "completed") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "✅ Complete!",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}