package com.example.data.repository

import android.content.Context
import com.example.alarm.AlarmScheduler
import com.example.data.db.AppDatabase
import com.example.data.db.SampleData
import com.example.data.model.BreakConfig
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig
import com.example.data.model.TimetableExportData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScheduleRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    private val scheduleDao = database.scheduleDao()
    private val schoolConfigDao = database.schoolConfigDao()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val exportAdapter = moshi.adapter(TimetableExportData::class.java).indent("  ")

    val allSchedules: Flow<List<ScheduleItem>> = scheduleDao.getAllSchedules()
    val schoolConfig: Flow<SchoolBellConfig?> = schoolConfigDao.getConfig()

    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<ScheduleItem>> {
        return scheduleDao.getSchedulesByDay(dayOfWeek)
    }

    suspend fun getSchoolConfigDirect(): SchoolBellConfig {
        return withContext(Dispatchers.IO) {
            val existing = schoolConfigDao.getConfigDirect()
            if (existing != null) {
                existing
            } else {
                val defaultConfig = SchoolBellConfig()
                schoolConfigDao.insertOrUpdateConfig(defaultConfig)
                defaultConfig
            }
        }
    }

    suspend fun saveSchoolConfig(config: SchoolBellConfig) {
        withContext(Dispatchers.IO) {
            schoolConfigDao.insertOrUpdateConfig(config)
            // Reschedule all active alarms with new timing/lead time
            val activeSchedules = scheduleDao.getActiveReminders()
            AlarmScheduler.rescheduleAllAlarms(context, activeSchedules, config)
        }
    }

    suspend fun insertSchedule(schedule: ScheduleItem): Long {
        return withContext(Dispatchers.IO) {
            val id = scheduleDao.insertSchedule(schedule)
            val config = getSchoolConfigDirect()
            val insertedItem = schedule.copy(id = id)
            if (insertedItem.isReminderEnabled) {
                AlarmScheduler.scheduleAlarm(context, insertedItem, config)
            }
            id
        }
    }

    suspend fun updateSchedule(schedule: ScheduleItem) {
        withContext(Dispatchers.IO) {
            scheduleDao.updateSchedule(schedule)
            val config = getSchoolConfigDirect()
            if (schedule.isReminderEnabled) {
                AlarmScheduler.scheduleAlarm(context, schedule, config)
            } else {
                AlarmScheduler.cancelAlarm(context, schedule.id)
            }
        }
    }

    suspend fun deleteSchedule(schedule: ScheduleItem) {
        withContext(Dispatchers.IO) {
            AlarmScheduler.cancelAlarm(context, schedule.id)
            scheduleDao.deleteSchedule(schedule)
        }
    }

    suspend fun loadSampleSMKData() {
        withContext(Dispatchers.IO) {
            val sampleSchedules = SampleData.getSampleSchedules()
            // Clear existing alarms
            val existing = scheduleDao.getAllSchedulesList()
            for (item in existing) {
                AlarmScheduler.cancelAlarm(context, item.id)
            }
            scheduleDao.clearAllSchedules()
            
            // Set standard SMK config (40 mins, 11 periods, breaks after JP 4 & JP 7)
            val smkConfig = SchoolBellConfig(
                schoolName = "SMK Negeri 1 Pringapus",
                startHour = 7,
                startMinute = 0,
                periodDurationMinutes = 40,
                totalPeriods = 11,
                breaks = listOf(
                    BreakConfig(afterPeriod = 4, durationMinutes = 20, name = "Istirahat ke-1"),
                    BreakConfig(afterPeriod = 7, durationMinutes = 50, name = "Istirahat ke-2")
                ),
                activeDays = listOf(1, 2, 3, 4, 5),
                leadTimeMinutes = 5,
                isSoundEnabled = true,
                soundPreset = "bell",
                isVibrationEnabled = true
            )
            schoolConfigDao.insertOrUpdateConfig(smkConfig)

            // Insert sample schedules
            scheduleDao.insertAllSchedules(sampleSchedules)
            val active = scheduleDao.getActiveReminders()
            AlarmScheduler.rescheduleAllAlarms(context, active, smkConfig)
        }
    }

    suspend fun exportToJson(): String {
        return withContext(Dispatchers.IO) {
            val config = getSchoolConfigDirect()
            val schedules = scheduleDao.getAllSchedulesList()
            val exportData = TimetableExportData(
                appName = "LishNgajarr",
                version = 1,
                exportedAt = System.currentTimeMillis(),
                schoolConfig = config,
                schedules = schedules
            )
            exportAdapter.toJson(exportData)
        }
    }

    suspend fun importFromJson(jsonString: String, replaceExisting: Boolean = true): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val data = exportAdapter.fromJson(jsonString)
                    ?: return@withContext Result.failure(IllegalArgumentException("Format file tidak valid."))

                if (replaceExisting) {
                    val existing = scheduleDao.getAllSchedulesList()
                    for (item in existing) {
                        AlarmScheduler.cancelAlarm(context, item.id)
                    }
                    scheduleDao.clearAllSchedules()
                }

                schoolConfigDao.insertOrUpdateConfig(data.schoolConfig)

                // Clean IDs if we are appending or resetting
                val schedulesToInsert = if (replaceExisting) {
                    data.schedules
                } else {
                    data.schedules.map { it.copy(id = 0) }
                }

                scheduleDao.insertAllSchedules(schedulesToInsert)
                val active = scheduleDao.getActiveReminders()
                AlarmScheduler.rescheduleAllAlarms(context, active, data.schoolConfig)

                Result.success(data.schedules.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
