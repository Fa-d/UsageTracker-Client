package dev.sadakat.screentimetracker.core.ui.utils

object CoreFormatters {
    fun formatPercentage(value: Float): String {
        return "${(value * 100).toInt()}%"
    }

    fun formatTime(hours: Float): String {
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            m > 0 -> "${m}m"
            else -> "0m"
        }
    }

    fun formatMetric(value: Int, unit: String = ""): String {
        return "$value$unit"
    }

    fun formatMetric(value: Float, unit: String = ""): String {
        return "${value.toInt()}$unit"
    }

    fun formatChange(change: Float, showSign: Boolean = true): String {
        val sign = if (showSign && change > 0) "+" else ""
        return "$sign${change.toInt()}%"
    }
}