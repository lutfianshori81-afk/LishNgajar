package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig
import com.example.ui.components.TimetableGridView
import com.example.ui.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onAddScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSchedules by viewModel.allSchedules.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val todayStatus by viewModel.todayStatus.collectAsState()

    var viewMode by remember { mutableIntStateOf(0) } // 0 = Agenda Harian, 1 = Tabel Matriks

    val daySchedules = allSchedules
        .filter { it.dayOfWeek == selectedDay }
        .sortedBy { it.startPeriod }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Live Status Banner (Next Class / Current Class)
            if (todayStatus.currentClass != null || todayStatus.nextClass != null || todayStatus.isBreakTime) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            todayStatus.currentClass != null -> MaterialTheme.colorScheme.primaryContainer
                            todayStatus.isBreakTime -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (todayStatus.isBreakTime) Icons.Default.Coffee else Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            when {
                                todayStatus.currentClass != null -> {
                                    val current = todayStatus.currentClass!!
                                    Text(
                                        text = "Sedang Berlangsung: ${current.subjectName} (${current.className})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "JP ${current.startPeriod}-${current.endPeriod} • Ruang ${current.room}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                todayStatus.isBreakTime -> {
                                    Text(
                                        text = "Waktu Istirahat: ${todayStatus.breakName ?: "Istirahat"}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (todayStatus.nextClass != null) {
                                        val next = todayStatus.nextClass!!
                                        Text(
                                            text = "Pelajaran berikutnya: ${next.subjectName} di Ruang ${next.room}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                todayStatus.nextClass != null -> {
                                    val next = todayStatus.nextClass!!
                                    val timeStr = schoolConfig.getPeriodStartTimeStrForDay(next.dayOfWeek, next.startPeriod)
                                    val minutes = todayStatus.minutesUntilNext ?: schoolConfig.leadTimeMinutes
                                    Text(
                                        text = "🔔 $minutes menit lagi: ${next.subjectName} (${next.className})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Mulai jam $timeStr WIB • Ruang ${next.room}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // View Mode Toggle (Agenda Harian vs Tabel Matriks)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = viewMode,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        modifier = Modifier.testTag("tab_agenda"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Agenda Harian", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        modifier = Modifier.testTag("tab_matrix"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CalendarViewMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Tabel Jadwal (Matrix)", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }

            // CONTENT
            if (viewMode == 0) {
                // AGENDA HARIAN VIEW
                Column(modifier = Modifier.fillMaxSize()) {
                    // Day Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (day in 1..6) {
                            val count = allSchedules.count { it.dayOfWeek == day }
                            FilterChip(
                                selected = selectedDay == day,
                                onClick = { viewModel.setSelectedDay(day) },
                                label = {
                                    Text(
                                        text = "${SchoolBellConfig.getDayName(day)}${if (count > 0) " ($count)" else ""}",
                                        fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("day_chip_$day")
                            )
                        }
                    }

                    if (daySchedules.isEmpty()) {
                        // Empty State for selected day
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Tidak Ada Jadwal di Hari ${SchoolBellConfig.getDayName(selectedDay)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tekan tombol + di bawah untuk menambahkan jadwal mengajar hari ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { viewModel.openAddSchedule(defaultDay = selectedDay) }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Jadwal ${SchoolBellConfig.getDayName(selectedDay)}")
                                }
                            }
                        }
                    } else {
                        // List of classes on the selected day
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            items(daySchedules, key = { it.id }) { item ->
                                ScheduleCard(
                                    item = item,
                                    schoolConfig = schoolConfig,
                                    onEditClick = { viewModel.openEditSchedule(item) },
                                    onToggleReminder = { viewModel.toggleReminder(item) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            } else {
                // TABEL JADWAL / MATRIX GRID VIEW (As seen on user photo)
                TimetableGridView(
                    schedules = allSchedules,
                    schoolConfig = schoolConfig,
                    onCellClick = { day, period ->
                        viewModel.openAddSchedule(defaultDay = day, defaultStartPeriod = period)
                    },
                    onScheduleClick = { item ->
                        viewModel.openEditSchedule(item)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Floating Action Button to Add New Schedule
        FloatingActionButton(
            onClick = {
                viewModel.openAddSchedule(defaultDay = selectedDay)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_schedule_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal Mengajar")
        }
    }
}

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    schoolConfig: SchoolBellConfig,
    onEditClick: () -> Unit,
    onToggleReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex.ifBlank { "#3F51B5" }))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val timeRangeStr = schoolConfig.formatMultiPeriodTimeForDay(item.dayOfWeek, item.startPeriod, item.endPeriod)
    val periodStr = if (item.startPeriod == item.endPeriod) {
        "Jam ke-${item.startPeriod}"
    } else {
        "Jam ke-${item.startPeriod} - ${item.endPeriod} (${item.totalPeriodCount} JP)"
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("schedule_card_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left colored stripe
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(115.dp)
                    .background(cardColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Header Row (Subject Name & Reminder Toggle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.subjectName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.className.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = cardColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = item.className,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onToggleReminder,
                        modifier = Modifier.size(32.dp).testTag("reminder_toggle_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle Pengingat",
                            tint = if (item.isReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Time and Period Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$periodStr • $timeRangeStr",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Room and Notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.room.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Ruang: ${item.room}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
