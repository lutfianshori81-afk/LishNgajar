package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.ScheduleItem
import com.example.data.model.SchoolBellConfig

object NotificationHelper {

    const val CHANNEL_ID = "lishngajarr_schedule_channel"
    const val CHANNEL_NAME = "Pengingat Jadwal Mengajar"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat jadwal mengajar sebelum jam pelajaran dimulai"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showScheduleReminderNotification(
        context: Context,
        schedule: ScheduleItem,
        config: SchoolBellConfig,
        customMinutesLeft: Int? = null
    ) {
        createNotificationChannel(context)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SCHEDULE_ID", schedule.id)
            putExtra("EXTRA_DAY_OF_WEEK", schedule.dayOfWeek)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            schedule.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutesBefore = customMinutesLeft ?: config.leadTimeMinutes
        val timeRangeStr = config.formatMultiPeriodTimeForDay(schedule.dayOfWeek, schedule.startPeriod, schedule.endPeriod)
        val startTimeStr = config.getPeriodStartTimeStrForDay(schedule.dayOfWeek, schedule.startPeriod)

        val periodText = if (schedule.startPeriod == schedule.endPeriod) {
            "Jam ke-${schedule.startPeriod}"
        } else {
            "Jam ke-${schedule.startPeriod} - ${schedule.endPeriod}"
        }

        val title = "🔔 $minutesBefore Menit Lagi: ${schedule.subjectName} (${schedule.className})"
        val contentText = "$periodText ($timeRangeStr) • Ruang ${schedule.room}"

        val bigText = buildString {
            append("Mata Pelajaran: ${schedule.subjectName}\n")
            append("Kelas: ${schedule.className}\n")
            append("Waktu: $periodText ($timeRangeStr)\n")
            append("Dimulai Jam: $startTimeStr WIB\n")
            append("Ruang: ${schedule.room}")
            if (schedule.notes.isNotBlank()) {
                append("\nCatatan: ${schedule.notes}")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF3F51B5.toInt())
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "Buka Jadwal",
                pendingIntent
            )

        notificationManager.notify(schedule.id.toInt(), builder.build())

        // Play sound and vibration if enabled in config
        if (config.isSoundEnabled) {
            SoundHelper.playPresetSound(context, config.soundPreset, config.customSoundUri)
        }
        if (config.isVibrationEnabled) {
            SoundHelper.vibratePhone(context)
        }
    }

    fun showTestNotification(context: Context, config: SchoolBellConfig) {
        createNotificationChannel(context)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🔔 Tes Pengingat: LishNgajarr Aktif!"
        val contentText = "Notifikasi pengingat ${config.leadTimeMinutes} menit sebelum mengajar siap berfungsi."
        val soundName = when (config.soundPreset) {
            "custom" -> "MP3 Sendiri (${config.customSoundName ?: "Kustom"})"
            else -> config.soundPreset
        }
        val bigText = "Pengaturan Pengingat Berhasil!\n" +
                "• Suara: ${if (config.isSoundEnabled) soundName else "Mati"}\n" +
                "• Getar: ${if (config.isVibrationEnabled) "Aktif" else "Mati"}\n" +
                "• Pengingat Muncul: ${config.leadTimeMinutes} menit sebelum jam pelajaran dimulai."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF3F51B5.toInt())

        notificationManager.notify(9999, builder.build())

        if (config.isSoundEnabled) {
            SoundHelper.playPresetSound(context, config.soundPreset, config.customSoundUri)
        }
        if (config.isVibrationEnabled) {
            SoundHelper.vibratePhone(context)
        }
    }
}
