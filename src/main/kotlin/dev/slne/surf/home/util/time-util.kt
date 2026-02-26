package dev.slne.surf.home.util

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Duration.userContent(): String {
    if (this.isNegative()) return "Unbegrenzt"

    var remaining = this

    val days = remaining.inWholeDays
    remaining -= days.toDuration(DurationUnit.DAYS)

    val hours = remaining.inWholeHours
    remaining -= hours.toDuration(DurationUnit.HOURS)

    val minutes = remaining.inWholeMinutes
    remaining -= minutes.toDuration(DurationUnit.MINUTES)

    val seconds = remaining.inWholeSeconds
    remaining -= seconds.toDuration(DurationUnit.SECONDS)

    val millis = remaining.inWholeMilliseconds

    val parts = mutableListOf<String>()
    if (days > 0) parts.add("$days ${if (days == 1L) "Tag" else "Tage"}")
    if (hours > 0) parts.add("$hours ${if (hours == 1L) "Stunde" else "Stunden"}")
    if (minutes > 0) parts.add("$minutes ${if (minutes == 1L) "Minute" else "Minuten"}")
    if (seconds > 0) parts.add("$seconds ${if (seconds == 1L) "Sekunde" else "Sekunden"}")
    if (parts.isEmpty() && millis > 0) parts.add("$millis Millisekunden")

    return parts.joinToString(", ")
}