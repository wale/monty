package au.id.wale.monty.util

import org.threeten.extra.AmountFormats
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.time.Duration
import java.time.Instant
import java.util.*


fun calculateUptime(startTime: Instant, currentTime: Instant = Instant.now()): String {
    val timestamp = Duration.between(startTime, currentTime)

    return AmountFormats.wordBased(timestamp, Locale.getDefault())
}

fun formatRAMUsage(): String {
    var bytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current())
}
