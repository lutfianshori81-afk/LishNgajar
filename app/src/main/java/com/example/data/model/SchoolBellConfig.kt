package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.Locale

@JsonClass(generateAdapter = true)
@Entity(tableName = "school_config")
data class SchoolBellConfig(
    @PrimaryKey
    val id: Int = 1,
    val schoolName: String = "SMK Negeri 1 Pringapus",
    val startHour: Int = 7, // 07:00
    val startMinute: Int = 0,
    val periodDurationMinutes: Int = 40, // 40 menit per JP reguler (Senin - Kamis, Sabtu)
    val totalPeriods: Int = 11, // JP 1 s/d JP 11 reguler
    val breaks: List<BreakConfig> = listOf(
        BreakConfig(afterPeriod = 4, durationMinutes = 20, name = "Istirahat ke-1"),
        BreakConfig(afterPeriod = 7, durationMinutes = 50, name = "Istirahat ke-2")
    ),
    val activeDays: List<Int> = listOf(1, 2, 3, 4, 5), // 1=Senin..5=Jumat (6=Sabtu)
    
    // Friday Custom Timing (Khusus Hari Jumat)
    val fridayCustomEnabled: Boolean = true,
    val fridayStartHour: Int = 7, // 07:00
    val fridayStartMinute: Int = 0,
    val fridayPeriodDurationMinutes: Int = 35, // 35 menit per JP pada hari Jumat
    val fridayTotalPeriods: Int = 6, // 6 JP pada hari Jumat (misal selesai sebelum Sholat Jumat)
    val fridayBreaks: List<BreakConfig> = listOf(
        BreakConfig(afterPeriod = 3, durationMinutes = 20, name = "Istirahat Jumat")
    ),

    // Notification & Custom MP3 preferences
    val leadTimeMinutes: Int = 5, // Notifikasi muncul 5 menit sebelum kelas dimulai
    val isSoundEnabled: Boolean = true,
    val soundPreset: String = "bell", // "bell", "chime", "whistle", "ping", "alarm", "system", "custom"
    val customSoundUri: String? = null, // Path file MP3 kustom dari perangkat
    val customSoundName: String? = null, // Nama file MP3 (misal: Bel_Sekolah.mp3)
    val isVibrationEnabled: Boolean = true,

    // Theme Mode: "SYSTEM", "LIGHT", "DARK"
    val themeMode: String = "SYSTEM"
) {
    /**
     * Calculates the start and end time in minutes from midnight for a given period on a specific day.
     * Takes Friday custom settings into account when dayOfWeek == 5 and fridayCustomEnabled is true.
     */
    fun getPeriodTimeRangeForDay(dayOfWeek: Int, period: Int): Pair<Int, Int> {
        val isFridaySpecial = dayOfWeek == 5 && fridayCustomEnabled
        val effectiveStartHour = if (isFridaySpecial) fridayStartHour else startHour
        val effectiveStartMinute = if (isFridaySpecial) fridayStartMinute else startMinute
        val effectiveDuration = if (isFridaySpecial) fridayPeriodDurationMinutes else periodDurationMinutes
        val effectiveBreaks = if (isFridaySpecial) fridayBreaks else breaks

        val schoolStartMins = effectiveStartHour * 60 + effectiveStartMinute
        var currentMins = schoolStartMins

        val sortedBreaks = effectiveBreaks.sortedBy { it.afterPeriod }

        for (p in 1..period) {
            val periodStart = currentMins
            val periodEnd = periodStart + effectiveDuration

            if (p == period) {
                return Pair(periodStart, periodEnd)
            }

            currentMins = periodEnd
            val matchingBreak = sortedBreaks.find { it.afterPeriod == p }
            if (matchingBreak != null) {
                currentMins += matchingBreak.durationMinutes
            }
        }

        return Pair(currentMins, currentMins + effectiveDuration)
    }

    /**
     * Default calculation assuming regular day
     */
    fun getPeriodTimeRange(period: Int): Pair<Int, Int> {
        return getPeriodTimeRangeForDay(1, period)
    }

    /**
     * Calculates the time range across multiple periods on a specific day.
     */
    fun getMultiPeriodTimeRangeForDay(dayOfWeek: Int, startPeriod: Int, endPeriod: Int): Pair<Int, Int> {
        val startRange = getPeriodTimeRangeForDay(dayOfWeek, startPeriod)
        val endRange = getPeriodTimeRangeForDay(dayOfWeek, endPeriod.coerceAtLeast(startPeriod))
        return Pair(startRange.first, endRange.second)
    }

    fun getMultiPeriodTimeRange(startPeriod: Int, endPeriod: Int): Pair<Int, Int> {
        return getMultiPeriodTimeRangeForDay(1, startPeriod, endPeriod)
    }

    fun formatPeriodTime(period: Int): String {
        val (startMins, endMins) = getPeriodTimeRange(period)
        return "${formatMinutes(startMins)} - ${formatMinutes(endMins)}"
    }

    fun formatPeriodTimeForDay(dayOfWeek: Int, period: Int): String {
        val (startMins, endMins) = getPeriodTimeRangeForDay(dayOfWeek, period)
        return "${formatMinutes(startMins)} - ${formatMinutes(endMins)}"
    }

    fun formatMultiPeriodTime(startPeriod: Int, endPeriod: Int): String {
        val (startMins, endMins) = getMultiPeriodTimeRange(startPeriod, endPeriod)
        return "${formatMinutes(startMins)} - ${formatMinutes(endMins)}"
    }

    fun formatMultiPeriodTimeForDay(dayOfWeek: Int, startPeriod: Int, endPeriod: Int): String {
        val (startMins, endMins) = getMultiPeriodTimeRangeForDay(dayOfWeek, startPeriod, endPeriod)
        return "${formatMinutes(startMins)} - ${formatMinutes(endMins)}"
    }

    fun getPeriodStartTimeStr(period: Int): String {
        val (startMins, _) = getPeriodTimeRange(period)
        return formatMinutes(startMins)
    }

    fun getPeriodStartTimeStrForDay(dayOfWeek: Int, period: Int): String {
        val (startMins, _) = getPeriodTimeRangeForDay(dayOfWeek, period)
        return formatMinutes(startMins)
    }

    fun getPeriodEndTimeStr(period: Int): String {
        val (_, endMins) = getPeriodTimeRange(period)
        return formatMinutes(endMins)
    }

    fun getPeriodEndTimeStrForDay(dayOfWeek: Int, period: Int): String {
        val (_, endMins) = getPeriodTimeRangeForDay(dayOfWeek, period)
        return formatMinutes(endMins)
    }

    fun getTotalPeriodsForDay(dayOfWeek: Int): Int {
        return if (dayOfWeek == 5 && fridayCustomEnabled) fridayTotalPeriods else totalPeriods
    }

    fun getBreaksForDay(dayOfWeek: Int): List<BreakConfig> {
        return if (dayOfWeek == 5 && fridayCustomEnabled) fridayBreaks else breaks
    }

    fun getDurationForDay(dayOfWeek: Int): Int {
        return if (dayOfWeek == 5 && fridayCustomEnabled) fridayPeriodDurationMinutes else periodDurationMinutes
    }

    companion object {
        fun formatMinutes(totalMinutes: Int): String {
            val hours = (totalMinutes / 60) % 24
            val mins = totalMinutes % 60
            return String.format(Locale.getDefault(), "%02d:%02d", hours, mins)
        }

        fun getDayName(dayIndex: Int): String {
            return when (dayIndex) {
                1 -> "Senin"
                2 -> "Selasa"
                3 -> "Rabu"
                4 -> "Kamis"
                5 -> "Jumat"
                6 -> "Sabtu"
                7 -> "Minggu"
                else -> "Hari $dayIndex"
            }
        }

        fun getDayShortName(dayIndex: Int): String {
            return when (dayIndex) {
                1 -> "Sen"
                2 -> "Sel"
                3 -> "Rab"
                4 -> "Kam"
                5 -> "Jum"
                6 -> "Sab"
                7 -> "Min"
                else -> "$dayIndex"
            }
        }
    }
}
