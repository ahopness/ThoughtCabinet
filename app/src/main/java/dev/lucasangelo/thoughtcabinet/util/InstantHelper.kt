package dev.lucasangelo.thoughtcabinet.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatInstant(instant: Instant, pattern: String): String {
    val formater = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return formater.format(instant.atZone(ZoneId.systemDefault()))
}