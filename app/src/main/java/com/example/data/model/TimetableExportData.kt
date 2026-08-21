package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimetableExportData(
    val appName: String = "LishNgajarr",
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val schoolConfig: SchoolBellConfig,
    val schedules: List<ScheduleItem>
)
