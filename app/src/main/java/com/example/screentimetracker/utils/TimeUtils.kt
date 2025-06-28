package com.example.screentimetracker.utils

fun millisToReadableTime(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    return "%dh %02dm".format(hours, minutes)
}
