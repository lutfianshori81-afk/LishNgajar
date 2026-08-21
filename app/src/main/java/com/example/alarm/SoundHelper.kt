package com.example.alarm

import android.content.Context
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

object SoundHelper {

    private const val TAG = "SoundHelper"
    private var activeMediaPlayer: MediaPlayer? = null

    val AVAILABLE_PRESETS = listOf(
        SoundPreset("bell", "Bel Sekolah (School Bell)", "Nada lonceng ganda klasik tanda masuk kelas"),
        SoundPreset("chime", "Lonceng Lembut (Gentle Chime)", "Harmoni nada lembut menenangkan"),
        SoundPreset("whistle", "Peluit Pengajar (Teacher Whistle)", "Nada peluit nada ganda dinamis"),
        SoundPreset("ping", "Kristal Ping (Crystal Tone)", "Nada jernih ringkas dan tegas"),
        SoundPreset("alarm", "Beep Perhatian (Alert Beep)", "Bunyi peringatan berulang 3 kali"),
        SoundPreset("system", "Nada Sistem Default", "Menggunakan nada notifikasi standar perangkat")
    )

    data class SoundPreset(val key: String, val title: String, val description: String)

    /**
     * Copies chosen MP3/Audio file from Uri into app internal files directory
     * so it can be reliably played at any time (even after device reboot).
     * Returns Pair(absoluteFilePath, originalFileName)
     */
    fun saveCustomAudioFromUri(context: Context, sourceUri: Uri): Pair<String, String>? {
        return try {
            var fileName = "custom_bell.mp3"
            // Query original filename
            val cursor: Cursor? = context.contentResolver.query(sourceUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex) ?: fileName
                    }
                }
            }

            val destFile = File(context.filesDir, "custom_notification_sound.mp3")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            Pair(destFile.absolutePath, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save custom audio: ${e.message}", e)
            null
        }
    }

    fun playPresetSound(context: Context, presetKey: String, customSoundUri: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Stop any previously playing sound
                stopSound()

                when {
                    presetKey == "custom" && !customSoundUri.isNullOrBlank() -> {
                        playCustomAudioFile(context, customSoundUri)
                    }
                    presetKey == "bell" -> playSynthesizedChime(listOf(523.25, 659.25, 783.99, 1046.50), listOf(180, 180, 220, 450))
                    presetKey == "chime" -> playSynthesizedChime(listOf(440.0, 554.37, 659.25, 880.0), listOf(150, 150, 180, 500))
                    presetKey == "whistle" -> playSynthesizedChime(listOf(880.0, 1174.66, 880.0, 1318.51), listOf(120, 200, 100, 300))
                    presetKey == "ping" -> playSynthesizedChime(listOf(1046.50, 1318.51), listOf(120, 400))
                    presetKey == "alarm" -> playSynthesizedChime(listOf(880.0, 0.0, 880.0, 0.0, 880.0), listOf(120, 80, 120, 80, 250))
                    presetKey == "system" -> playSystemNotificationSound(context)
                    else -> playSynthesizedChime(listOf(523.25, 659.25, 783.99), listOf(150, 150, 350))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing sound: ${e.message}", e)
                playSystemNotificationSound(context)
            }
        }
    }

    private fun playCustomAudioFile(context: Context, filePathOrUri: String) {
        try {
            val file = File(filePathOrUri)
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                if (file.exists()) {
                    setDataSource(file.absolutePath)
                } else {
                    setDataSource(context, Uri.parse(filePathOrUri))
                }
                prepare()
                setOnCompletionListener {
                    it.release()
                    if (activeMediaPlayer == it) activeMediaPlayer = null
                }
                start()
            }
            activeMediaPlayer = mp
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing custom MP3, falling back to bell chime: ${e.message}", e)
            playSynthesizedChime(listOf(523.25, 659.25, 783.99, 1046.50), listOf(180, 180, 220, 450))
        }
    }

    fun stopSound() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            activeMediaPlayer = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun playSystemNotificationSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Synthesizes audio tones smoothly using AudioTrack with ADSR envelope to prevent clicks
     */
    private fun playSynthesizedChime(frequencies: List<Double>, durationsMs: List<Int>) {
        val sampleRate = 44100
        val totalMs = durationsMs.sum()
        val totalSamples = (sampleRate * totalMs) / 1000
        val audioData = ShortArray(totalSamples)

        var sampleOffset = 0
        for (i in frequencies.indices) {
            val freq = frequencies[i]
            val durationMs = durationsMs[i]
            val toneSamples = (sampleRate * durationMs) / 1000

            if (freq <= 0.0) {
                // Silence
                for (s in 0 until toneSamples) {
                    if (sampleOffset + s < audioData.size) {
                        audioData[sampleOffset + s] = 0
                    }
                }
            } else {
                val attackSamples = (sampleRate * 0.015).toInt().coerceAtMost(toneSamples / 4)
                val decaySamples = (sampleRate * 0.08).toInt().coerceAtMost(toneSamples / 3)

                for (s in 0 until toneSamples) {
                    val angle = 2.0 * Math.PI * s / (sampleRate / freq)
                    var amplitude = sin(angle)

                    // Add slight harmonic overtone for warmth
                    amplitude += 0.3 * sin(2.0 * angle)

                    // Envelope: Attack, Sustain, Decay
                    val envelope = when {
                        s < attackSamples -> (s.toDouble() / attackSamples)
                        s > toneSamples - decaySamples -> ((toneSamples - s).toDouble() / decaySamples)
                        else -> 1.0 - 0.2 * (s - attackSamples).toDouble() / (toneSamples - attackSamples)
                    }

                    val sampleValue = (amplitude * envelope * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
                    if (sampleOffset + s < audioData.size) {
                        audioData[sampleOffset + s] = sampleValue
                    }
                }
            }
            sampleOffset += toneSamples
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(audioData.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(audioData, 0, audioData.size)
        audioTrack.play()
    }

    fun vibratePhone(context: Context, pattern: LongArray = longArrayOf(0, 300, 200, 300)) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
