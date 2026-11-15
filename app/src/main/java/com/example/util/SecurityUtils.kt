package com.example.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SecurityUtils {

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun normalizeRecoveryAnswer(answer: String): String {
        return answer.trim().lowercase(Locale.US)
    }

    fun hashRecoveryAnswer(answer: String): String {
        return sha256(normalizeRecoveryAnswer(answer))
    }
}

object TimeUtils {

    fun currentDateKey(calendar: Calendar = Calendar.getInstance()): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(calendar.time)
    }

    fun currentFormattedClock(calendar: Calendar = Calendar.getInstance()): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.US)
        return formatter.format(calendar.time)
    }

    fun currentMinutesOfDay(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    fun dayShortLabel(calendar: Calendar): String {
        val formatter = SimpleDateFormat("EEE", Locale.US)
        return formatter.format(calendar.time)
    }

    fun isWithinBedtime(
        nowMinutesOfDay: Int,
        startMinutes: Int,
        endMinutes: Int
    ): Boolean {
        if (startMinutes == endMinutes) return false
        return if (startMinutes < endMinutes) {
            nowMinutesOfDay in startMinutes until endMinutes
        } else {
            // Overnight window (e.g., 20:00 -> 07:00)
            nowMinutesOfDay >= startMinutes || nowMinutesOfDay < endMinutes
        }
    }

    fun formatMinutesOfDay(minutesOfDay: Int): String {
        val normalized = ((minutesOfDay % 1440) + 1440) % 1440
        val hour24 = normalized / 60
        val minute = normalized % 60
        val amPm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
    }

    fun formatDurationMinutes(totalMinutes: Int): String {
        val safe = totalMinutes.coerceAtLeast(0)
        val hours = safe / 60
        val mins = safe % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    fun formatCountdownSeconds(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val hours = safe / 3600
        val mins = (safe % 3600) / 60
        val secs = safe % 60
        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, mins)
            mins > 0 -> String.format(Locale.US, "%dm %02ds", mins, secs)
            else -> String.format(Locale.US, "%ds", secs)
        }
    }
}
