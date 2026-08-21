package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig
import com.example.ui.theme.SubjectColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditScheduleDialog(
    initialSchedule: ScheduleItem?,
    schoolConfig: SchoolBellConfig,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit,
    onDelete: ((ScheduleItem) -> Unit)? = null
) {
    if (initialSchedule == null) return

    val isEdit = initialSchedule.id != 0L

    var dayOfWeek by remember { mutableIntStateOf(initialSchedule.dayOfWeek) }
    var subjectName by remember { mutableStateOf(initialSchedule.subjectName) }
    var className by remember { mutableStateOf(initialSchedule.className) }
    var room by remember { mutableStateOf(initialSchedule.room) }
    val maxDayPeriods = schoolConfig.getTotalPeriodsForDay(dayOfWeek)
    var startPeriod by remember { mutableIntStateOf(initialSchedule.startPeriod.coerceIn(1, maxDayPeriods)) }
    var endPeriod by remember { mutableIntStateOf(initialSchedule.endPeriod.coerceIn(startPeriod, maxDayPeriods)) }
    var colorHex by remember { mutableStateOf(initialSchedule.colorHex.ifBlank { "#3F51B5" }) }
    var notes by remember { mutableStateOf(initialSchedule.notes) }
    var isReminderEnabled by remember { mutableStateOf(initialSchedule.isReminderEnabled) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Edit Jadwal Mengajar" else "Tambah Jadwal Mengajar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isEdit && onDelete != null) {
                    IconButton(
                        onClick = { onDelete(initialSchedule) },
                        modifier = Modifier.testTag("delete_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Jadwal",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Day Selector Chips
                Column {
                    Text(
                        text = "Pilih Hari",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (d in 1..6) {
                            FilterChip(
                                selected = dayOfWeek == d,
                                onClick = { dayOfWeek = d },
                                label = { Text(SchoolBellConfig.getDayName(d)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Subject Name Field
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = {
                        subjectName = it
                        errorMessage = null
                    },
                    label = { Text("Mata Pelajaran (Mapel)") },
                    placeholder = { Text("Contoh: KIK, Informatika, Matematika") },
                    leadingIcon = {
                        Icon(Icons.Default.Book, contentDescription = "Mapel")
                    },
                    singleLine = true,
                    isError = errorMessage != null && subjectName.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input")
                )

                // Class Name Field
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Kelas / Tingkat") },
                    placeholder = { Text("Contoh: XII DKV 1, X B 4, XI RPL") },
                    leadingIcon = {
                        Icon(Icons.Default.People, contentDescription = "Kelas")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_input")
                )

                // Room Field
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Keterangan Ruang / Lab") },
                    placeholder = { Text("Contoh: D3, Lab DKV, Ruang 12") },
                    leadingIcon = {
                        Icon(Icons.Default.MeetingRoom, contentDescription = "Ruang")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_input")
                )

                // Period Selection (Jam ke Berapa - Jam ke Berapa)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Jam Pelajaran (JP)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Period
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mulai: JP $startPeriod",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (startPeriod > 1) {
                                                startPeriod--
                                            }
                                        },
                                        enabled = startPeriod > 1,
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("-", fontSize = 18.sp)
                                    }
                                    Text(
                                        text = "$startPeriod",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            if (startPeriod < maxDayPeriods) {
                                                startPeriod++
                                                if (endPeriod < startPeriod) endPeriod = startPeriod
                                            }
                                        },
                                        enabled = startPeriod < maxDayPeriods,
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("+", fontSize = 18.sp)
                                    }
                                }
                            }

                            // End Period
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Selesai: JP $endPeriod",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (endPeriod > startPeriod) {
                                                endPeriod--
                                            }
                                        },
                                        enabled = endPeriod > startPeriod,
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("-", fontSize = 18.sp)
                                    }
                                    Text(
                                        text = "$endPeriod",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            if (endPeriod < maxDayPeriods) {
                                                endPeriod++
                                            }
                                        },
                                        enabled = endPeriod < maxDayPeriods,
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("+", fontSize = 18.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val timeRangeStr = schoolConfig.formatMultiPeriodTimeForDay(dayOfWeek, startPeriod, endPeriod)
                        Text(
                            text = "⏰ Estimasi Waktu: $timeRangeStr (${(endPeriod - startPeriod + 1)} JP)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Color Tag Picker
                Column {
                    Text(
                        text = "Warna Penanda Jadwal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SubjectColors.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Color(0xFF3F51B5)
                            }
                            val isSelected = colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Materi / Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: Bawa modul praktikum, tugas kelompok") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Reminder Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Notifikasi Pengingat",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${schoolConfig.leadTimeMinutes} menit sebelum jam pelajaran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subjectName.trim().isBlank()) {
                        errorMessage = "Mohon isi nama mata pelajaran"
                        return@Button
                    }
                    val itemToSave = initialSchedule.copy(
                        dayOfWeek = dayOfWeek,
                        subjectName = subjectName.trim(),
                        className = className.trim(),
                        room = room.trim(),
                        startPeriod = startPeriod,
                        endPeriod = endPeriod,
                        colorHex = colorHex,
                        notes = notes.trim(),
                        isReminderEnabled = isReminderEnabled
                    )
                    onSave(itemToSave)
                },
                modifier = Modifier.testTag("save_schedule_button")
            ) {
                Text(if (isEdit) "Simpan Perubahan" else "Tambah Jadwal")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_schedule_button")
            ) {
                Text("Batal")
            }
        }
    )
}
