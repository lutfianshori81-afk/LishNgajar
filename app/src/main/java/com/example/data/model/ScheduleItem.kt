package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // Calendar.MONDAY (2) to Calendar.SUNDAY (1) or 1=Senin..7=Minggu
    val subjectName: String, // Mata Pelajaran (e.g. "KIK", "Informatika")
    val className: String, // Kelas (e.g. "XII DKV 1", "X B 4")
    val room: String, // Ruang / Lab (e.g. "Lab DKV", "D3", "D1")
    val startPeriod: Int, // Jam ke mulai (1-based, e.g. 3)
    val endPeriod: Int, // Jam ke selesai (1-based, e.g. 4)
    val colorHex: String = "#3F51B5", // Hex color for timetable display
    val notes: String = "", // Catatan tambahan (e.g. "Materi UI/UX")
    val isReminderEnabled: Boolean = true // Whether alarm notification is active
) {
    val totalPeriodCount: Int
        get() = (endPeriod - startPeriod + 1).coerceAtLeast(1)
}
