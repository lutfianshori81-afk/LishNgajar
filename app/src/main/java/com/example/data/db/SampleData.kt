package com.example.data.db

import com.example.data.model.ScheduleItem

object SampleData {
    fun getSampleSchedules(): List<ScheduleItem> {
        return listOf(
            // SENIN (1)
            ScheduleItem(
                id = 1,
                dayOfWeek = 1, // Senin
                subjectName = "Upacara Bendera",
                className = "Semua Kelas / Guru",
                room = "Lapangan Utama",
                startPeriod = 1,
                endPeriod = 1,
                colorHex = "#455A64", // Slate Blue-Grey
                notes = "Petugas upacara & pembina apel pagi",
                isReminderEnabled = true
            ),

            // RABU (3)
            ScheduleItem(
                id = 2,
                dayOfWeek = 3, // Rabu
                subjectName = "KIK",
                className = "XII DKV 3",
                room = "D1",
                startPeriod = 7,
                endPeriod = 7,
                colorHex = "#7986CB", // Soft Indigo / Lavender
                notes = "Komunikasi Industri Kreatif - Pengenalan Modul",
                isReminderEnabled = true
            ),
            ScheduleItem(
                id = 3,
                dayOfWeek = 3, // Rabu
                subjectName = "KIK",
                className = "XII DKV 3",
                room = "D1",
                startPeriod = 8,
                endPeriod = 11,
                colorHex = "#7986CB", // Soft Indigo / Lavender
                notes = "Praktik Portofolio Desain Komunikasi Visual",
                isReminderEnabled = true
            ),

            // KAMIS (4)
            ScheduleItem(
                id = 4,
                dayOfWeek = 4, // Kamis
                subjectName = "KIK",
                className = "XII DKV 1",
                room = "D3",
                startPeriod = 3,
                endPeriod = 4,
                colorHex = "#E57373", // Soft Coral / Red
                notes = "Kreativitas & Ilustrasi Digital",
                isReminderEnabled = true
            ),
            ScheduleItem(
                id = 5,
                dayOfWeek = 4, // Kamis
                subjectName = "KIK",
                className = "XII DKV 1",
                room = "D3",
                startPeriod = 5,
                endPeriod = 7,
                colorHex = "#E57373", // Soft Coral / Red
                notes = "Praktikum Rendering dan Asesmen Format D3",
                isReminderEnabled = true
            ),

            // JUMAT (5)
            ScheduleItem(
                id = 6,
                dayOfWeek = 5, // Jumat
                subjectName = "Literasi",
                className = "Semua Siswa",
                room = "Kelas Masing-masing",
                startPeriod = 1,
                endPeriod = 1,
                colorHex = "#4DB6AC", // Teal
                notes = "Kegiatan pembiasaan membaca pagi",
                isReminderEnabled = true
            ),
            ScheduleItem(
                id = 7,
                dayOfWeek = 5, // Jumat
                subjectName = "Informatika",
                className = "X B 4",
                room = "Lab DKV",
                startPeriod = 2,
                endPeriod = 4,
                colorHex = "#BA68C8", // Soft Orchid / Magenta
                notes = "Pemrograman Dasar & Pengenalan Algoritma",
                isReminderEnabled = true
            ),
            ScheduleItem(
                id = 8,
                dayOfWeek = 5, // Jumat
                subjectName = "Informatika",
                className = "X B 4",
                room = "Lab DKV",
                startPeriod = 5,
                endPeriod = 6,
                colorHex = "#BA68C8", // Soft Orchid / Magenta
                notes = "Praktik Lab Komputer & Asesmen Formatif",
                isReminderEnabled = true
            )
        )
    }
}
