package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig
import com.example.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun appDayToCalendarDay(day: Int): Int {
        return when (day) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }

    fun calculateNextTriggerTime(schedule: ScheduleItem, config: SchoolBellConfig): Long {
        val (startMins, _) = config.getPeriodTimeRangeForDay(schedule.dayOfWeek, schedule.startPeriod)
        var reminderMins = startMins - config.leadTimeMinutes
        var dayOffset = 0
        if (reminderMins < 0) {
            // E.g. reminder falls on previous day late night
            reminderMins += 24 * 60
            dayOffset = -1
        }

        val reminderHour = (reminderMins / 60) % 24
        val reminderMinute = reminderMins % 60

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, appDayToCalendarDay(schedule.dayOfWeek))
            if (dayOffset != 0) {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            set(Calendar.HOUR_OF_DAY, reminderHour)
            set(Calendar.MINUTE, reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed this week, advance by 7 days
        if (calendar.timeInMillis <= System.currentTimeMillis() + 1000) {
            calendar.add(Calendar.DAY_OF_YEAR, 7)
        }

        return calendar.timeInMillis
    }

    fun scheduleAlarm(context: Context, schedule: ScheduleItem, config: SchoolBellConfig) {
        if (!schedule.isReminderEnabled) {
            cancelAlarm(context, schedule.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(AlarmReceiver.EXTRA_SUBJECT_NAME, schedule.subjectName)
            putExtra(AlarmReceiver.EXTRA_CLASS_NAME, schedule.className)
            putExtra(AlarmReceiver.EXTRA_ROOM, schedule.room)
            putExtra(AlarmReceiver.EXTRA_START_PERIOD, schedule.startPeriod)
            putExtra(AlarmReceiver.EXTRA_END_PERIOD, schedule.endPeriod)
            putExtra(AlarmReceiver.EXTRA_NOTES, schedule.notes)
            putExtra(AlarmReceiver.EXTRA_DAY_OF_WEEK, schedule.dayOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(schedule, config)
        Log.d(TAG, "Scheduling alarm for ${schedule.subjectName} (${schedule.className}) at $triggerTime")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact alarm: ${e.message}", e)
        }
    }

    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAllAlarms(context: Context, schedules: List<ScheduleItem>, config: SchoolBellConfig) {
        for (schedule in schedules) {
            if (schedule.isReminderEnabled) {
                scheduleAlarm(context, schedule, config)
            } else {
                cancelAlarm(context, schedule.id)
            }
        }
    }
}
