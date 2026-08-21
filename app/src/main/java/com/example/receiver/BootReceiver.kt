package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.AlarmScheduler
import com.example.data.db.AppDatabase
import com.example.data.model.SchoolBellConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Boot/Time change event received: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val config = db.schoolConfigDao().getConfigDirect() ?: SchoolBellConfig()
                val activeSchedules = db.scheduleDao().getActiveReminders()

                Log.d("BootReceiver", "Rescheduling ${activeSchedules.size} active alarms...")
                AlarmScheduler.rescheduleAllAlarms(context, activeSchedules, config)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to reschedule on boot: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
