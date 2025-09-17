package dev.sadakat.screentimetracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class TimelineEvent(
    val id: String,
    val appPackageName: String,
    val appName: String,
    val startTime: Long,
    val duration: Long,
    val color: Color,
    val icon: String = "📱"
)

@Composable
fun InteractiveTimeline(
    events: List<TimelineEvent>,
    modifier: Modifier = Modifier,
    selectedEvent: TimelineEvent? = null,
    onEventSelected: (TimelineEvent?) -> Unit = {},
    showHours: Boolean = true,
    timeRangeStart: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000L,
    timeRangeEnd: Long = System.currentTimeMillis()
) {
    val density = LocalDensity.current
    var selectedEventState by remember { mutableStateOf(selectedEvent) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Header with time range
        TimelineHeader(
            startTime = timeRangeStart,
            endTime = timeRangeEnd,
            showHours = showHours
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main timeline canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(events) {
                    detectTapGestures { offset ->
                        val timelineWidth = size.width.toFloat()
                        val timeRange = timeRangeEnd - timeRangeStart
                        val clickTime = timeRangeStart + ((offset.x / timelineWidth) * timeRange).toLong()
                        
                        // Find closest event to click
                        val clickedEvent = events.minByOrNull { event ->
                            kotlin.math.abs(event.startTime - clickTime)
                        }
                        
                        if (clickedEvent != null && 
                            kotlin.math.abs(clickedEvent.startTime - clickTime) < timeRange / 20) {
                            selectedEventState = if (selectedEventState == clickedEvent) null else clickedEvent
                            onEventSelected(selectedEventState)
                        }
                    }
                }
        ) {
            drawTimeline(
                events = events,
                timeRangeStart = timeRangeStart,
                timeRangeEnd = timeRangeEnd,
                selectedEvent = selectedEventState,
                showHours = showHours
            )
        }
        
        // Selected event details
        selectedEventState?.let { event ->
            Spacer(modifier = Modifier.height(16.dp))
            SelectedEventCard(
                event = event,
                onDismiss = { 
                    selectedEventState = null
                    onEventSelected(null)
                }
            )
        }
    }
}

@Composable
private fun TimelineHeader(
    startTime: Long,
    endTime: Long,
    showHours: Boolean
) {
    val timeFormat = if (showHours) "HH:mm" else "MMM dd"
    val formatter = SimpleDateFormat(timeFormat, Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatter.format(Date(startTime)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = formatter.format(Date(endTime)),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun DrawScope.drawTimeline(
    events: List<TimelineEvent>,
    timeRangeStart: Long,
    timeRangeEnd: Long,
    selectedEvent: TimelineEvent?,
    showHours: Boolean
) {
    val timelineHeight = size.height
    val timelineWidth = size.width
    val timeRange = timeRangeEnd - timeRangeStart
    val baselineY = timelineHeight * 0.6f
    val trackHeight = 40.dp.toPx()
    
    // Draw timeline base
    drawRoundRect(
        color = Color.Gray.copy(alpha = 0.2f),
        topLeft = Offset(0f, baselineY - trackHeight / 2f),
        size = Size(timelineWidth, trackHeight),
        cornerRadius = CornerRadius(trackHeight / 2f)
    )
    
    // Draw hour markers if showing hours
    if (showHours) {
        drawHourMarkers(timeRangeStart, timeRangeEnd, timelineWidth, baselineY)
    }
    
    // Group overlapping events by time slots
    val eventSlots = groupEventsByTimeSlots(events, timeRangeStart, timeRange, timelineWidth)
    
    // Draw events
    eventSlots.forEach { slot ->
        slot.events.forEachIndexed { index, event ->
            val startX = (((event.startTime - timeRangeStart).toFloat() / timeRange) * timelineWidth)
                .coerceIn(0f, timelineWidth - 10.dp.toPx())
            val eventWidth = ((event.duration.toFloat() / timeRange) * timelineWidth)
                .coerceAtLeast(8.dp.toPx())
                .coerceAtMost(timelineWidth - startX)
            
            val slotHeight = trackHeight / slot.events.size
            val eventY = baselineY - trackHeight / 2f + (index * slotHeight)
            
            val isSelected = event == selectedEvent
            val eventColor = if (isSelected) event.color else event.color.copy(alpha = 0.8f)
            val eventHeight = if (isSelected) slotHeight * 1.2f else slotHeight * 0.8f
            
            // Draw event block
            drawRoundRect(
                color = eventColor,
                topLeft = Offset(startX, eventY + (slotHeight - eventHeight) / 2f),
                size = Size(eventWidth, eventHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            
            // Draw app icon/emoji above if selected or long enough
            if (isSelected || eventWidth > 30.dp.toPx()) {
                drawContext.canvas.nativeCanvas.drawText(
                    event.icon,
                    startX + eventWidth / 2f,
                    eventY - 10.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 14.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

private fun DrawScope.drawHourMarkers(
    timeRangeStart: Long,
    timeRangeEnd: Long,
    timelineWidth: Float,
    baselineY: Float
) {
    val timeRange = timeRangeEnd - timeRangeStart
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timeRangeStart
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.HOUR_OF_DAY, 1) // Start from next hour
    }
    
    while (calendar.timeInMillis < timeRangeEnd) {
        val markerX = (((calendar.timeInMillis - timeRangeStart).toFloat() / timeRange) * timelineWidth)
        
        drawLine(
            color = Color.Gray.copy(alpha = 0.4f),
            start = Offset(markerX, baselineY - 30.dp.toPx()),
            end = Offset(markerX, baselineY + 30.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        
        // Hour label
        val hourText = String.format("%02d:00", calendar.get(Calendar.HOUR_OF_DAY))
        drawContext.canvas.nativeCanvas.drawText(
            hourText,
            markerX,
            baselineY + 50.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
        
        calendar.add(Calendar.HOUR_OF_DAY, 1)
    }
}

private data class EventSlot(
    val startTime: Long,
    val endTime: Long,
    val events: List<TimelineEvent>
)

private fun groupEventsByTimeSlots(
    events: List<TimelineEvent>,
    timeRangeStart: Long,
    timeRange: Long,
    timelineWidth: Float
): List<EventSlot> {
    val sortedEvents = events.sortedBy { it.startTime }
    val slots = mutableListOf<EventSlot>()
    val slotDuration = timeRange / (timelineWidth / 150f).roundToInt() // ~150px per slot (approximately 50dp)
    
    var currentSlotStart = timeRangeStart
    while (currentSlotStart < timeRangeStart + timeRange) {
        val currentSlotEnd = currentSlotStart + slotDuration
        val slotEvents = sortedEvents.filter { event ->
            event.startTime < currentSlotEnd && event.startTime + event.duration > currentSlotStart
        }
        
        if (slotEvents.isNotEmpty()) {
            slots.add(EventSlot(currentSlotStart, currentSlotEnd, slotEvents))
        }
        
        currentSlotStart = currentSlotEnd
    }
    
    return slots
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedEventCard(
    event: TimelineEvent,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = event.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.icon,
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = event.appName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Text(
                        text = formatDuration(event.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val minutes = (durationMillis / 60000).toInt()
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${remainingMinutes}m"
    }
}