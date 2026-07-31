package com.openfinds.app.core.ui.format

import java.util.concurrent.TimeUnit

/** A short, human "3m ago" / "2h ago" style relative timestamp for last-seen labels. */
fun relativeTime(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val deltaMs = (nowMillis - epochMillis).coerceAtLeast(0)
    return when {
        deltaMs < TimeUnit.MINUTES.toMillis(1) -> "just now"
        deltaMs < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(deltaMs)}m ago"
        deltaMs < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(deltaMs)}h ago"
        deltaMs < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(deltaMs)}d ago"
        else -> "${TimeUnit.MILLISECONDS.toDays(deltaMs) / 7}w ago"
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) "${bytes} B" else String.format("%.1f %s", value, units[unitIndex])
}

fun formatDuration(millis: Long): String {
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60
    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        append("${minutes}m")
    }
}
