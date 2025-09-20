package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.data.local.entities.DigitalPet
import dev.sadakat.screentimetracker.data.local.entities.PetMood
import dev.sadakat.screentimetracker.data.local.entities.PetStats
import dev.sadakat.screentimetracker.data.local.entities.PetType
import dev.sadakat.screentimetracker.utils.DigitalPetManager

@Composable
fun DigitalPetCard(
    pet: DigitalPet,
    petStats: PetStats,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInteract: () -> Unit = {},
    onFeed: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet_animation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPetClick() },
        colors = CardDefaults.cardColors(
            containerColor = getPetCardColor(petStats.mood)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with pet name and level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = pet.petType.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "Lv.${petStats.level}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pet Avatar with mood animation
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle with sparkle effect for thriving pets
                if (petStats.mood == PetMood.THRIVING) {
                    Canvas(
                        modifier = Modifier.size(80.dp)
                    ) {
                        drawSparkleEffect(sparkleAlpha)
                    }
                }
                
                Text(
                    text = petStats.getEvolutionEmoji(pet.petType),
                    fontSize = 48.sp,
                    modifier = Modifier.scale(pulseScale)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mood and wellness score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${petStats.mood.emoji} ${petStats.mood.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${petStats.wellnessScore}% Wellness",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = getWellnessScoreColor(petStats.wellnessScore)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Experience progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "EXP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${petStats.experienceToNextLevel} to next level",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = { calculateExpProgress(petStats) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row (Health, Happiness, Energy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatMeter(
                    label = "Health",
                    value = petStats.health,
                    emoji = "❤️",
                    color = Color(0xFFE57373)
                )
                StatMeter(
                    label = "Happy",
                    value = petStats.happiness,
                    emoji = "😊",
                    color = Color(0xFFFFB74D)
                )
                StatMeter(
                    label = "Energy",
                    value = petStats.energy,
                    emoji = "⚡",
                    color = Color(0xFF81C784)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Motivational message
            Text(
                text = DigitalPetManager.getMotivationalMessage(petStats, pet.petType),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
            
            // Wellness streak (if any)
            if (pet.wellnessStreakDays > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔥 ${pet.wellnessStreakDays} day wellness streak!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier
                        .background(
                            Color(0xFFFF6B35).copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Interaction buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onInteract,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("👋 Pat", fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onFeed,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("🍎 Feed", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatMeter(
    label: String,
    value: Int,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = emoji,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        
        // Circular progress indicator
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                drawCircularProgress(value, color)
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun DrawScope.drawCircularProgress(
    value: Int,
    color: Color
) {
    val strokeWidth = 4.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // Background circle
    drawCircle(
        color = color.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
    )
    
    // Progress arc
    val sweepAngle = (value / 100f) * 360f
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
}

private fun DrawScope.drawSparkleEffect(alpha: Float) {
    val sparklePositions = listOf(
        Offset(size.width * 0.2f, size.height * 0.3f),
        Offset(size.width * 0.8f, size.height * 0.2f),
        Offset(size.width * 0.1f, size.height * 0.7f),
        Offset(size.width * 0.9f, size.height * 0.8f),
        Offset(size.width * 0.5f, size.height * 0.1f)
    )
    
    sparklePositions.forEach { position ->
        drawCircle(
            color = Color.Yellow.copy(alpha = alpha),
            radius = 3.dp.toPx(),
            center = position
        )
    }
}

private fun getPetCardColor(mood: PetMood): Color {
    return when (mood) {
        PetMood.THRIVING -> Color(0xFFF1F8E9) // Light green
        PetMood.HAPPY -> Color(0xFFFFF3E0) // Light orange
        PetMood.CONTENT -> Color(0xFFE3F2FD) // Light blue
        PetMood.CONCERNED -> Color(0xFFFFF8E1) // Light yellow
        PetMood.SICK -> Color(0xFFFFEBEE) // Light red
        PetMood.SLEEPING -> Color(0xFFF3E5F5) // Light purple
    }
}

private fun getWellnessScoreColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50) // Green
        score >= 75 -> Color(0xFF8BC34A) // Light green
        score >= 60 -> Color(0xFFFFEB3B) // Yellow
        score >= 40 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun calculateExpProgress(petStats: PetStats): Float {
    val currentLevelExp = petStats.experiencePoints
    val nextLevelExp = petStats.experiencePoints + petStats.experienceToNextLevel
    val prevLevelExp = nextLevelExp - (100 * (1.2 * petStats.level)).toInt()
    
    val currentLevelProgress = currentLevelExp - prevLevelExp
    val levelExpRange = nextLevelExp - prevLevelExp
    
    return if (levelExpRange > 0) {
        (currentLevelProgress.toFloat() / levelExpRange.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
}