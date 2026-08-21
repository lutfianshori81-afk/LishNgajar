package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.SoundHelper
import com.example.data.model.BreakConfig
import com.example.data.model.SchoolBellConfig
import com.example.ui.components.ExportImportDialog
import com.example.ui.viewmodel.ScheduleViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentConfig by viewModel.schoolConfig.collectAsState()

    var schoolName by remember(currentConfig) { mutableStateOf(currentConfig.schoolName) }
    var startHour by remember(currentConfig) { mutableIntStateOf(currentConfig.startHour) }
    var startMinute by remember(currentConfig) { mutableIntStateOf(currentConfig.startMinute) }
    var periodDuration by remember(currentConfig) { mutableIntStateOf(currentConfig.periodDurationMinutes) }
    var totalPeriods by remember(currentConfig) { mutableIntStateOf(currentConfig.totalPeriods) }
    var leadTime by remember(currentConfig) { mutableIntStateOf(currentConfig.leadTimeMinutes) }
    var isSoundEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isSoundEnabled) }
    var soundPreset by remember(currentConfig) { mutableStateOf(currentConfig.soundPreset) }
    var isVibrationEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isVibrationEnabled) }
    var breaksList by remember(currentConfig) { mutableStateOf(currentConfig.breaks) }

    // Friday specific settings
    var fridayCustomEnabled by remember(currentConfig) { mutableStateOf(currentConfig.fridayCustomEnabled) }
    var fridayStartHour by remember(currentConfig) { mutableIntStateOf(currentConfig.fridayStartHour) }
    var fridayStartMinute by remember(currentConfig) { mutableIntStateOf(currentConfig.fridayStartMinute) }
    var fridayPeriodDuration by remember(currentConfig) { mutableIntStateOf(currentConfig.fridayPeriodDurationMinutes) }
    var fridayTotalPeriods by remember(currentConfig) { mutableIntStateOf(currentConfig.fridayTotalPeriods) }
    var fridayBreaksList by remember(currentConfig) { mutableStateOf(currentConfig.fridayBreaks) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomAudioFile(uri)
            soundPreset = "custom"
        }
    }

    var showAddBreakDialog by remember { mutableStateOf(false) }
    var showAddFridayBreakDialog by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }
    var showConfirmSampleDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header: Pengaturan Sekolah & Jam Belajar Umum (Senin - Kamis, Sabtu)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pengaturan Sekolah & Jam Reguler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // School Name
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("Nama Sekolah / Lembaga") },
                    placeholder = { Text("SMK Negeri 1 Pringapus") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_school_name")
                )

                // Start Time (Jam Mulai JP 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Jam Masuk (JP 1)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format("%02d:%02d WIB", startHour, startMinute),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Hour control
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jam", style = MaterialTheme.typography.labelSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { if (startHour > 5) startHour-- },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("-") }
                                Text(
                                    text = String.format("%02d", startHour),
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = { if (startHour < 12) startHour++ },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("+") }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Minute control
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Menit", style = MaterialTheme.typography.labelSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { startMinute = (startMinute - 5 + 60) % 60 },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("-") }
                                Text(
                                    text = String.format("%02d", startMinute),
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = { startMinute = (startMinute + 5) % 60 },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("+") }
                            }
                        }
                    }
                }

                // JP Duration & Total JP Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Duration per JP
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Durasi per 1 JP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (periodDuration > 15) periodDuration -= 5 },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("-") }
                            Text(
                                text = "$periodDuration mnt",
                                modifier = Modifier.padding(horizontal = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            OutlinedButton(
                                onClick = { if (periodDuration < 90) periodDuration += 5 },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }

                    // Total JP per Day
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Jam Reguler",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (totalPeriods > 4) totalPeriods-- },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("-") }
                            Text(
                                text = "$totalPeriods JP",
                                modifier = Modifier.padding(horizontal = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            OutlinedButton(
                                onClick = { if (totalPeriods < 16) totalPeriods++ },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }
                }
            }
        }

        // Section: Pengaturan Jam Istirahat Reguler
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Istirahat Hari Reguler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddBreakDialog = true },
                        modifier = Modifier.testTag("add_break_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp)
                    }
                }

                Text(
                    text = "Atur waktu istirahat yang diletakkan setelah jam ke berapa & durasinya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (breaksList.isEmpty()) {
                    Text(
                        text = "Belum ada jam istirahat yang dikonfigurasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    breaksList.sortedBy { it.afterPeriod }.forEachIndexed { index, brk ->
                        OutlinedCard(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = brk.name.ifBlank { "Istirahat ke-${index + 1}" },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Setelah JP ke-${brk.afterPeriod} • Durasi ${brk.durationMinutes} Menit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        breaksList = breaksList.filter { it.id != brk.id }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Istirahat",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Pengaturan Jam Manual Khusus Hari Jumat (Special Friday Schedule)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (fridayCustomEnabled) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                text = "Pengaturan Khusus Hari Jumat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Jam masuk, durasi & istirahat khusus Jumat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = fridayCustomEnabled,
                        onCheckedChange = { fridayCustomEnabled = it },
                        modifier = Modifier.testTag("friday_schedule_toggle")
                    )
                }

                if (fridayCustomEnabled) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Friday Start Time (Jam Mulai JP 1 Jumat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Jam Masuk Hari Jumat (JP 1)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format("%02d:%02d WIB", fridayStartHour, fridayStartMinute),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Hour control
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Jam", style = MaterialTheme.typography.labelSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(
                                        onClick = { if (fridayStartHour > 5) fridayStartHour-- },
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) { Text("-") }
                                    Text(
                                        text = String.format("%02d", fridayStartHour),
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                    OutlinedButton(
                                        onClick = { if (fridayStartHour < 12) fridayStartHour++ },
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) { Text("+") }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Minute control
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Menit", style = MaterialTheme.typography.labelSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(
                                        onClick = { fridayStartMinute = (fridayStartMinute - 5 + 60) % 60 },
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) { Text("-") }
                                    Text(
                                        text = String.format("%02d", fridayStartMinute),
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                    OutlinedButton(
                                        onClick = { fridayStartMinute = (fridayStartMinute + 5) % 60 },
                                        modifier = Modifier.size(36.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) { Text("+") }
                                }
                            }
                        }
                    }

                    // Friday JP Duration & Total JP Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Duration per JP Friday
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Durasi JP Jumat",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { if (fridayPeriodDuration > 15) fridayPeriodDuration -= 5 },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("-") }
                                Text(
                                    text = "$fridayPeriodDuration mnt",
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                OutlinedButton(
                                    onClick = { if (fridayPeriodDuration < 90) fridayPeriodDuration += 5 },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("+") }
                            }
                        }

                        // Total JP Friday
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Total JP Jumat",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { if (fridayTotalPeriods > 2) fridayTotalPeriods-- },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("-") }
                                Text(
                                    text = "$fridayTotalPeriods JP",
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                OutlinedButton(
                                    onClick = { if (fridayTotalPeriods < 14) fridayTotalPeriods++ },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("+") }
                            }
                        }
                    }

                    // Friday Breaks
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Istirahat Khusus Hari Jumat",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            FilledTonalButton(
                                onClick = { showAddFridayBreakDialog = true },
                                modifier = Modifier.testTag("add_friday_break_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah", fontSize = 11.sp)
                            }
                        }

                        if (fridayBreaksList.isEmpty()) {
                            Text(
                                text = "Belum ada istirahat hari Jumat (misal Sholat Jumat/Pulang Cepat).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            fridayBreaksList.sortedBy { it.afterPeriod }.forEachIndexed { index, brk ->
                                OutlinedCard(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = brk.name.ifBlank { "Istirahat Jumat ke-${index + 1}" },
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Setelah JP ke-${brk.afterPeriod} • Durasi ${brk.durationMinutes} Menit",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                fridayBreaksList = fridayBreaksList.filter { it.id != brk.id }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Istirahat Jumat",
                                                tint = MaterialTheme.colorScheme.error
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

        // Section: Pengaturan Notifikasi & Suara Pengingat (Termasuk MP3 Kustom)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pengingat & Suara Notifikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Lead Time selector (5 menit sebelum pelajaran dimulai)
                Column {
                    Text(
                        text = "Waktu Muncul Notifikasi Pengingat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Notifikasi berisi: Mapel, Jam ke, Jam mulai, & Ruang",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 5, 10, 15).forEach { mins ->
                            FilterChip(
                                selected = leadTime == mins,
                                onClick = { leadTime = mins },
                                label = { Text("$mins Menit Sebelum") }
                            )
                        }
                    }
                }

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Suara Notifikasi", fontWeight = FontWeight.SemiBold)
                            Text("Mainkan nada saat pengingat berbunyi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = { isSoundEnabled = it },
                        modifier = Modifier.testTag("sound_toggle")
                    )
                }

                // Sound Preset Selection & Custom MP3
                if (isSoundEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pilih Nada Pengingat:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Built-in presets
                        SoundHelper.AVAILABLE_PRESETS.forEach { preset ->
                            val isSelected = soundPreset == preset.key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { soundPreset = preset.key }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { soundPreset = preset.key }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(
                                            text = preset.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.previewSound(preset.key)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Dengarkan Nada",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // Custom MP3 Audio Option
                        val isCustomSelected = soundPreset == "custom"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCustomSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else Color.Transparent
                                )
                                .padding(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isCustomSelected,
                                        onClick = {
                                            if (!currentConfig.customSoundUri.isNullOrBlank()) {
                                                soundPreset = "custom"
                                            } else {
                                                audioPickerLauncher.launch("audio/*")
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Audiotrack,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "File MP3 / Audio Sendiri",
                                                fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        Text(
                                            text = if (!currentConfig.customSoundName.isNullOrBlank()) {
                                                "File: ${currentConfig.customSoundName}"
                                            } else {
                                                "Pilih file MP3/WAV dari penyimpanan HP"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (!currentConfig.customSoundName.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!currentConfig.customSoundUri.isNullOrBlank()) {
                                        IconButton(
                                            onClick = {
                                                viewModel.previewSound("custom", currentConfig.customSoundUri)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Putar MP3 Kustom",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { audioPickerLauncher.launch("audio/*") },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp).testTag("pick_mp3_button")
                                    ) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (currentConfig.customSoundUri.isNullOrBlank()) "Pilih MP3" else "Ganti", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Getar Saat Pengingat", fontWeight = FontWeight.SemiBold)
                            Text("Bergetar untuk menarik perhatian pengajar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = { isVibrationEnabled = it }
                    )
                }

                // Test Notification Button
                OutlinedButton(
                    onClick = {
                        val tempConfig = currentConfig.copy(
                            leadTimeMinutes = leadTime,
                            isSoundEnabled = isSoundEnabled,
                            soundPreset = soundPreset,
                            isVibrationEnabled = isVibrationEnabled,
                            fridayCustomEnabled = fridayCustomEnabled,
                            fridayStartHour = fridayStartHour,
                            fridayStartMinute = fridayStartMinute,
                            fridayPeriodDurationMinutes = fridayPeriodDuration,
                            fridayTotalPeriods = fridayTotalPeriods,
                            fridayBreaks = fridayBreaksList
                        )
                        viewModel.saveSchoolConfig(tempConfig)
                        viewModel.testNotification()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_notification_button")
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim Notifikasi Tes Sekarang (Uji Coba)")
                }
            }
        }

        // Section: Ekspor & Impor dan Backup
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kelola Data & Cadangan Jadwal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Jadwal mengajar dan seluruh pengaturan dapat diekspor menjadi file/kode JSON cadangan atau diimpor kembali dengan mudah.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showExportImportDialog = true },
                        modifier = Modifier.weight(1f).testTag("open_export_import_button")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor / Impor")
                    }

                    OutlinedButton(
                        onClick = { showConfirmSampleDialog = true },
                        modifier = Modifier.weight(1f).testTag("load_sample_data_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Muat Contoh")
                    }
                }
            }
        }

        // SAVE BUTTON (FAB-style bottom bar)
        Button(
            onClick = {
                val newConfig = currentConfig.copy(
                    schoolName = schoolName.trim().ifBlank { "SMK Negeri 1 Pringapus" },
                    startHour = startHour,
                    startMinute = startMinute,
                    periodDurationMinutes = periodDuration,
                    totalPeriods = totalPeriods,
                    breaks = breaksList,
                    leadTimeMinutes = leadTime,
                    isSoundEnabled = isSoundEnabled,
                    soundPreset = soundPreset,
                    isVibrationEnabled = isVibrationEnabled,
                    fridayCustomEnabled = fridayCustomEnabled,
                    fridayStartHour = fridayStartHour,
                    fridayStartMinute = fridayStartMinute,
                    fridayPeriodDurationMinutes = fridayPeriodDuration,
                    fridayTotalPeriods = fridayTotalPeriods,
                    fridayBreaks = fridayBreaksList
                )
                viewModel.saveSchoolConfig(newConfig)
                Toast.makeText(context, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_all_settings_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Pengaturan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Add Regular Break Dialog
    if (showAddBreakDialog) {
        var breakNameInput by remember { mutableStateOf("Istirahat ke-${breaksList.size + 1}") }
        var afterPeriodInput by remember { mutableIntStateOf(4) }
        var durationInput by remember { mutableIntStateOf(20) }

        AlertDialog(
            onDismissRequest = { showAddBreakDialog = false },
            title = { Text("Tambah Istirahat Reguler") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = breakNameInput,
                        onValueChange = { breakNameInput = it },
                        label = { Text("Nama Istirahat") },
                        placeholder = { Text("Istirahat ke-1 / ISHOMA") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            text = "Ditaruh setelah jam pelajaran ke:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (afterPeriodInput > 1) afterPeriodInput-- },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("-") }
                            Text(
                                text = "Setelah JP $afterPeriodInput",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { if (afterPeriodInput < totalPeriods - 1) afterPeriodInput++ },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }

                    Column {
                        Text(
                            text = "Durasi istirahat (menit):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (durationInput > 5) durationInput -= 5 },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("-") }
                            Text(
                                text = "$durationInput Menit",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { if (durationInput < 90) durationInput += 5 },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newBreak = BreakConfig(
                            id = UUID.randomUUID().toString(),
                            name = breakNameInput.trim().ifBlank { "Istirahat" },
                            afterPeriod = afterPeriodInput,
                            durationMinutes = durationInput
                        )
                        breaksList = breaksList + newBreak
                        showAddBreakDialog = false
                    }
                ) {
                    Text("Tambahkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBreakDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Add Friday Break Dialog
    if (showAddFridayBreakDialog) {
        var breakNameInput by remember { mutableStateOf("Istirahat Sholat Jumat") }
        var afterPeriodInput by remember { mutableIntStateOf(3) }
        var durationInput by remember { mutableIntStateOf(20) }

        AlertDialog(
            onDismissRequest = { showAddFridayBreakDialog = false },
            title = { Text("Tambah Istirahat Hari Jumat") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = breakNameInput,
                        onValueChange = { breakNameInput = it },
                        label = { Text("Nama Istirahat Jumat") },
                        placeholder = { Text("Istirahat Jumat / Snack") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            text = "Ditaruh setelah jam pelajaran ke:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (afterPeriodInput > 1) afterPeriodInput-- },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("-") }
                            Text(
                                text = "Setelah JP $afterPeriodInput",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { if (afterPeriodInput < fridayTotalPeriods - 1) afterPeriodInput++ },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }

                    Column {
                        Text(
                            text = "Durasi istirahat (menit):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (durationInput > 5) durationInput -= 5 },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("-") }
                            Text(
                                text = "$durationInput Menit",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(
                                onClick = { if (durationInput < 90) durationInput += 5 },
                                modifier = Modifier.size(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) { Text("+") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newBreak = BreakConfig(
                            id = UUID.randomUUID().toString(),
                            name = breakNameInput.trim().ifBlank { "Istirahat Jumat" },
                            afterPeriod = afterPeriodInput,
                            durationMinutes = durationInput
                        )
                        fridayBreaksList = fridayBreaksList + newBreak
                        showAddFridayBreakDialog = false
                    }
                ) {
                    Text("Tambahkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFridayBreakDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Confirm Load Sample Data
    if (showConfirmSampleDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSampleDialog = false },
            title = { Text("Muat Jadwal Contoh SMK?") },
            text = {
                Text("Tindakan ini akan memuat jadwal contoh sesuai SMK Negeri 1 Pringapus (Mapel KIK, Informatika, Upacara, Literasi, dan Lab DKV) serta mengatur durasi 40 menit, 2x istirahat, dan jadwal khusus Jumat.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadSampleSMKData()
                        showConfirmSampleDialog = false
                        Toast.makeText(context, "Jadwal contoh SMK dimuat!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Ya, Muat Contoh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSampleDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Export / Import Dialog
    if (showExportImportDialog) {
        ExportImportDialog(
            viewModel = viewModel,
            onDismiss = { showExportImportDialog = false }
        )
    }
}
