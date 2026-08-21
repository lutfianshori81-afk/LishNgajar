package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class BreakConfig(
    val id: String = UUID.randomUUID().toString(),
    val afterPeriod: Int, // e.g. 4 means break occurs after JP 4
    val durationMinutes: Int, // e.g. 20 (minutes)
    val name: String = "Istirahat" // e.g. "Istirahat ke-1", "Istirahat ke-2 / ISHOMA"
)
