package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BreakConfig
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig

sealed interface TimetableRowItem {
    data class PeriodRow(val period: Int) : TimetableRowItem
    data class BreakRow(val breakConfig: BreakConfig) : TimetableRowItem
}

@Composable
fun TimetableGridView(
    schedules: List<ScheduleItem>,
    schoolConfig: SchoolBellConfig,
    onCellClick: (day: Int, period: Int) -> Unit,
    onScheduleClick: (ScheduleItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDays = schoolConfig.activeDays.ifEmpty { listOf(1, 2, 3, 4, 5) }

    // Build list of rows interleaving periods and breaks
    val rows = mutableListOf<TimetableRowItem>()
    val maxPeriods = maxOf(schoolConfig.totalPeriods, schoolConfig.getTotalPeriodsForDay(5))
    val breakMap = schoolConfig.breaks.groupBy { it.afterPeriod }

    for (p in 1..maxPeriods) {
        rows.add(TimetableRowItem.PeriodRow(p))
        val breaksAfterThis = breakMap[p]
        if (breaksAfterThis != null) {
            for (b in breaksAfterThis) {
                rows.add(TimetableRowItem.BreakRow(b))
            }
        }
    }

    val headerColWidth = 100.dp
    val dayColWidth = 145.dp
    val rowHeight = 72.dp
    val breakRowHeight = 44.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // School banner header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(2.dp)
                    )
                    Text(
                        text = schoolConfig.schoolName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Ketuk kotak untuk edit/tambah",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scrollable 2D Grid Table
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val horizontalScrollState = rememberScrollState()
            val verticalScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState)
                    .verticalScroll(verticalScrollState)
            ) {
                // Table Header Row (Days)
                Row(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Top Left Corner cell (Jam / Hari)
                    Box(
                        modifier = Modifier
                            .width(headerColWidth)
                            .height(44.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Jam / Hari",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Days Columns (Senin, Selasa, Rabu, Kamis, Jumat, etc.)
                    for (day in activeDays) {
                        Box(
                            modifier = Modifier
                                .width(dayColWidth)
                                .height(44.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = SchoolBellConfig.getDayName(day),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Table Data Rows
                for (rowItem in rows) {
                    when (rowItem) {
                        is TimetableRowItem.BreakRow -> {
                            val brk = rowItem.breakConfig
                            val (_, periodEndMins) = schoolConfig.getPeriodTimeRange(brk.afterPeriod)
                            val breakEndMins = periodEndMins + brk.durationMinutes
                            val timeStr = "${SchoolBellConfig.formatMinutes(periodEndMins)} - ${SchoolBellConfig.formatMinutes(breakEndMins)}"

                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                // Break Header Label
                                Box(
                                    modifier = Modifier
                                        .width(headerColWidth)
                                        .height(breakRowHeight)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Istirahat",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Merged full-width break span
                                val totalBreakWidth = dayColWidth * activeDays.size
                                Box(
                                    modifier = Modifier
                                        .width(totalBreakWidth)
                                        .height(breakRowHeight)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${brk.name}  ($timeStr)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        is TimetableRowItem.PeriodRow -> {
                            val period = rowItem.period
                            val (startMins, endMins) = schoolConfig.getPeriodTimeRange(period)
                            val timeStr = "${SchoolBellConfig.formatMinutes(startMins)} - ${SchoolBellConfig.formatMinutes(endMins)}"

                            Row {
                                // Period Index & Time Column
                                Box(
                                    modifier = Modifier
                                        .width(headerColWidth)
                                        .height(rowHeight)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$period",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Day Cells
                                for (day in activeDays) {
                                    // Find if there is a schedule that covers this period on this day
                                    val matchingSchedule = schedules.find { s ->
                                        s.dayOfWeek == day && period in s.startPeriod..s.endPeriod
                                    }

                                    if (matchingSchedule != null) {
                                        val isFirstPeriodOfBlock = matchingSchedule.startPeriod == period
                                        val isLastPeriodOfBlock = matchingSchedule.endPeriod == period
                                        val bgHex = matchingSchedule.colorHex.ifBlank { "#3F51B5" }
                                        val blockColor = try {
                                            Color(android.graphics.Color.parseColor(bgHex))
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(dayColWidth)
                                                .height(rowHeight)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                                .background(blockColor.copy(alpha = 0.85f))
                                                .clickable { onScheduleClick(matchingSchedule) }
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                if (isFirstPeriodOfBlock) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = matchingSchedule.subjectName,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        if (matchingSchedule.isReminderEnabled) {
                                                            Icon(
                                                                imageVector = Icons.Default.Notifications,
                                                                contentDescription = "Pengingat Aktif",
                                                                tint = Color.White.copy(alpha = 0.9f),
                                                                modifier = Modifier.padding(start = 2.dp).height(12.dp)
                                                            )
                                                        }
                                                    }
                                                    if (matchingSchedule.className.isNotBlank()) {
                                                        Text(
                                                            text = matchingSchedule.className,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = Color.White.copy(alpha = 0.95f),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                if (matchingSchedule.startPeriod != matchingSchedule.endPeriod && !isFirstPeriodOfBlock && !isLastPeriodOfBlock) {
                                                    Text(
                                                        text = matchingSchedule.className,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White.copy(alpha = 0.95f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                if (isLastPeriodOfBlock && matchingSchedule.room.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color.Black.copy(alpha = 0.25f),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Ruang: ${matchingSchedule.room}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 9.sp,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty Slot (Tap to add schedule at this day & period)
                                        Box(
                                            modifier = Modifier
                                                .width(dayColWidth)
                                                .height(rowHeight)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                                .clickable { onCellClick(day, period) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Tambah jadwal di slot ini",
                                                tint = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
