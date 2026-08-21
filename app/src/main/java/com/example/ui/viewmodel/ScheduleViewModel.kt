package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.NotificationHelper
import com.example.alarm.SoundHelper
import com.example.data.model.BreakConfig
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig
import com.example.data.repository.ScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TodayClassStatus(
    val currentClass: ScheduleItem? = null,
    val nextClass: ScheduleItem? = null,
    val minutesUntilNext: Int? = null,
    val currentPeriod: Int? = null,
    val isBreakTime: Boolean = false,
    val breakName: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)

    val allSchedules: StateFlow<List<ScheduleItem>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schoolConfig: StateFlow<SchoolBellConfig> = repository.schoolConfig
        .combine(MutableStateFlow(SchoolBellConfig())) { config, default ->
            config ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolBellConfig())

    // Selected Day tab for Agenda view (1=Senin .. 7=Minggu)
    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Dialog state for adding or editing a schedule item
    private val _editingSchedule = MutableStateFlow<ScheduleItem?>(null)
    val editingSchedule: StateFlow<ScheduleItem?> = _editingSchedule.asStateFlow()

    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    // Live status of today's schedule
    private val _todayStatus = MutableStateFlow(TodayClassStatus())
    val todayStatus: StateFlow<TodayClassStatus> = _todayStatus.asStateFlow()

    // User feedback messages (Snackbar / Toast)
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            allSchedules.collect { list ->
                if (list.isEmpty()) {
                    // Check direct database and load sample SMK data if empty
                    val config = repository.getSchoolConfigDirect()
                    if (config.schoolName == "SMK Negeri 1 Pringapus") {
                        repository.loadSampleSMKData()
                    }
                }
                updateTodayStatus()
            }
        }

        // Start periodic check for current / next class status every 30 seconds
        viewModelScope.launch {
            while (true) {
                updateTodayStatus()
                delay(30_000)
            }
        }
    }

    fun setSelectedDay(day: Int) {
        _selectedDay.value = day
    }

    fun openAddSchedule(defaultDay: Int = _selectedDay.value, defaultStartPeriod: Int = 1) {
        _editingSchedule.value = ScheduleItem(
            dayOfWeek = defaultDay,
            subjectName = "",
            className = "",
            room = "",
            startPeriod = defaultStartPeriod,
            endPeriod = defaultStartPeriod,
            colorHex = "#3F51B5",
            notes = "",
            isReminderEnabled = true
        )
        _isAddEditOpen.value = true
    }

    fun openEditSchedule(item: ScheduleItem) {
        _editingSchedule.value = item
        _isAddEditOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditOpen.value = false
        _editingSchedule.value = null
    }

    fun saveSchedule(item: ScheduleItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertSchedule(item)
                _messageEvents.emit("Jadwal ${item.subjectName} (${item.className}) berhasil ditambahkan!")
            } else {
                repository.updateSchedule(item)
                _messageEvents.emit("Jadwal ${item.subjectName} berhasil diperbarui!")
            }
            closeAddEditDialog()
            updateTodayStatus()
        }
    }

    fun deleteSchedule(item: ScheduleItem) {
        viewModelScope.launch {
            repository.deleteSchedule(item)
            _messageEvents.emit("Jadwal ${item.subjectName} dihapus.")
            closeAddEditDialog()
            updateTodayStatus()
        }
    }

    fun toggleReminder(item: ScheduleItem) {
        viewModelScope.launch {
            val updated = item.copy(isReminderEnabled = !item.isReminderEnabled)
            repository.updateSchedule(updated)
            val status = if (updated.isReminderEnabled) "diaktifkan" else "dimatikan"
            _messageEvents.emit("Pengingat untuk ${item.subjectName} $status")
        }
    }

    fun saveSchoolConfig(config: SchoolBellConfig) {
        viewModelScope.launch {
            repository.saveSchoolConfig(config)
            _messageEvents.emit("Pengaturan jam pelajaran & istirahat disimpan!")
            updateTodayStatus()
        }
    }

    fun setCustomAudioFile(uri: android.net.Uri) {
        viewModelScope.launch {
            val savedAudio = SoundHelper.saveCustomAudioFromUri(getApplication(), uri)
            if (savedAudio != null) {
                val (path, fileName) = savedAudio
                val updatedConfig = schoolConfig.value.copy(
                    soundPreset = "custom",
                    customSoundUri = path,
                    customSoundName = fileName
                )
                repository.saveSchoolConfig(updatedConfig)
                _messageEvents.emit("Audio notifikasi kustom berhasil dipasang: $fileName")
                previewSound("custom", path)
            } else {
                _messageEvents.emit("Gagal membaca file audio kustom.")
            }
        }
    }

    fun testNotification() {
        viewModelScope.launch {
            val config = schoolConfig.value
            NotificationHelper.showTestNotification(getApplication(), config)
            _messageEvents.emit("Notifikasi pengingat tes dikirim!")
        }
    }

    fun previewSound(presetKey: String, customUri: String? = schoolConfig.value.customSoundUri) {
        SoundHelper.playPresetSound(getApplication(), presetKey, customUri)
    }

    fun stopSound() {
        SoundHelper.stopSound()
    }

    fun loadSampleSMKData() {
        viewModelScope.launch {
            repository.loadSampleSMKData()
            _messageEvents.emit("Jadwal contoh SMK Negeri 1 Pringapus berhasil dimuat!")
            updateTodayStatus()
        }
    }

    fun exportToJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportToJson()
            onResult(json)
        }
    }

    fun importFromJson(json: String, replaceExisting: Boolean, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(json, replaceExisting)
            result.fold(
                onSuccess = { count ->
                    _messageEvents.emit("Berhasil mengimpor $count jadwal!")
                    updateTodayStatus()
                    onDone(true, "Berhasil mengimpor $count jadwal pelajaran.")
                },
                onFailure = { error ->
                    val msg = "Gagal impor: ${error.message ?: "Format file tidak sesuai"}"
                    _messageEvents.emit(msg)
                    onDone(false, msg)
                }
            )
        }
    }

    fun updateTodayStatus() {
        val currentDay = getCurrentDayOfWeek()
        val cal = Calendar.getInstance()
        val currentMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val config = schoolConfig.value
        val todaySchedules = allSchedules.value.filter { it.dayOfWeek == currentDay }

        var activeClass: ScheduleItem? = null
        var nextClass: ScheduleItem? = null
        var minDiffToNext: Int? = null

        for (schedule in todaySchedules) {
            val (startMins, endMins) = config.getMultiPeriodTimeRangeForDay(currentDay, schedule.startPeriod, schedule.endPeriod)
            if (currentMins in startMins until endMins) {
                activeClass = schedule
            } else if (startMins > currentMins) {
                val diff = startMins - currentMins
                if (minDiffToNext == null || diff < minDiffToNext) {
                    minDiffToNext = diff
                    nextClass = schedule
                }
            }
        }

        // Check if currently during a break for today
        var isBreak = false
        var breakName: String? = null
        val todayBreaks = config.getBreaksForDay(currentDay)
        for (brk in todayBreaks) {
            val (_, periodEndMins) = config.getPeriodTimeRangeForDay(currentDay, brk.afterPeriod)
            val breakEndMins = periodEndMins + brk.durationMinutes
            if (currentMins in periodEndMins until breakEndMins) {
                isBreak = true
                breakName = "${brk.name} (s/d ${SchoolBellConfig.formatMinutes(breakEndMins)})"
                break
            }
        }

        _todayStatus.value = TodayClassStatus(
            currentClass = activeClass,
            nextClass = nextClass,
            minutesUntilNext = minDiffToNext,
            isBreakTime = isBreak,
            breakName = breakName
        )
    }

    companion object {
        fun getCurrentDayOfWeek(): Int {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
        }
    }
}
