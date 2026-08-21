package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.AlarmScheduler
import com.example.alarm.NotificationHelper
import com.example.data.db.AppDatabase
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER = "com.example.lishngajarr.ACTION_REMINDER"
        const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
        const val EXTRA_SUBJECT_NAME = "EXTRA_SUBJECT_NAME"
        const val EXTRA_CLASS_NAME = "EXTRA_CLASS_NAME"
        const val EXTRA_ROOM = "EXTRA_ROOM"
        const val EXTRA_START_PERIOD = "EXTRA_START_PERIOD"
        const val EXTRA_END_PERIOD = "EXTRA_END_PERIOD"
        const val EXTRA_NOTES = "EXTRA_NOTES"
        const val EXTRA_DAY_OF_WEEK = "EXTRA_DAY_OF_WEEK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId == -1L) return

        Log.d("AlarmReceiver", "Received alarm trigger for schedule ID: $scheduleId")

        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Mata Pelajaran"
        val className = intent.getStringExtra(EXTRA_CLASS_NAME) ?: "Kelas"
        val room = intent.getStringExtra(EXTRA_ROOM) ?: "-"
        val startPeriod = intent.getIntExtra(EXTRA_START_PERIOD, 1)
        val endPeriod = intent.getIntExtra(EXTRA_END_PERIOD, 1)
        val notes = intent.getStringExtra(EXTRA_NOTES) ?: ""
        val dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, 1)

        val fallbackSchedule = ScheduleItem(
            id = scheduleId,
            dayOfWeek = dayOfWeek,
            subjectName = subjectName,
            className = className,
            room = room,
            startPeriod = startPeriod,
            endPeriod = endPeriod,
            notes = notes,
            isReminderEnabled = true
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val config = db.schoolConfigDao().getConfigDirect() ?: SchoolBellConfig()
                val realSchedule = db.scheduleDao().getScheduleById(scheduleId) ?: fallbackSchedule

                if (realSchedule.isReminderEnabled) {
                    NotificationHelper.showScheduleReminderNotification(
                        context = context,
                        schedule = realSchedule,
                        config = config
                    )

                    // Re-schedule for next week
                    AlarmScheduler.scheduleAlarm(context, realSchedule, config)
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm: ${e.message}", e)
                NotificationHelper.showScheduleReminderNotification(
                    context = context,
                    schedule = fallbackSchedule,
                    config = SchoolBellConfig()
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
