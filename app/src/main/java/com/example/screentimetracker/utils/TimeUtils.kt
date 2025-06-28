package com.example.screentimetracker.utils

fun millisToReadableTime(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    val seconds = (millis / 1000) % 60

    return if (hours > 0) {
        "%dh %02dm".format(hours, minutes)
    } else if (minutes > 0) {
        "%dm %02ds".format(minutes, seconds)
    } else {
        "%ds".format(seconds)
    }
}
