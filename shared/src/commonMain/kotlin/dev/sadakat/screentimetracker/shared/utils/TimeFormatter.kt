package dev.sadakat.screentimetracker.shared.utils

object TimeFormatter {
    fun formatDuration(durationMs: Long): String {
        val minutes = durationMs / (1000 * 60)
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours > 0 -> "${hours}h ${remainingMinutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    fun formatPickups(pickups: Int): String {
        return "$pickups pickups"
    }

    fun formatWellnessScore(score: Float): String {
        return "${(score * 100).toInt()}%"
    }
}