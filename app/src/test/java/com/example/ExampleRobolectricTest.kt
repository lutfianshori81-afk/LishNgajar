package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BreakConfig
import com.example.data.model.SchoolBellConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies correct app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LishNgajarr", appName)
    }

    @Test
    fun `school bell config calculates regular period times correctly`() {
        val config = SchoolBellConfig(
            startHour = 7,
            startMinute = 0,
            periodDurationMinutes = 40,
            totalPeriods = 11,
            breaks = listOf(
                BreakConfig(afterPeriod = 4, durationMinutes = 20, name = "Istirahat 1"),
                BreakConfig(afterPeriod = 7, durationMinutes = 30, name = "Istirahat 2")
            )
        )

        // JP 1: 07:00 (420m) to 07:40 (460m)
        val (p1Start, p1End) = config.getPeriodTimeRangeForDay(1, 1)
        assertEquals(420, p1Start)
        assertEquals(460, p1End)
        assertEquals("07:00", SchoolBellConfig.formatMinutes(p1Start))
        assertEquals("07:40", SchoolBellConfig.formatMinutes(p1End))

        // JP 4: ends at 420 + 4*40 = 580 (09:40)
        val (p4Start, p4End) = config.getPeriodTimeRangeForDay(1, 4)
        assertEquals(580, p4End)

        // JP 5: starts after Istirahat 1 (20m) -> 580 + 20 = 600 (10:00) to 640 (10:40)
        val (p5Start, p5End) = config.getPeriodTimeRangeForDay(1, 5)
        assertEquals(600, p5Start)
        assertEquals(640, p5End)
        assertEquals("10:00", SchoolBellConfig.formatMinutes(p5Start))
        assertEquals("10:40", SchoolBellConfig.formatMinutes(p5End))
    }

    @Test
    fun `school bell config calculates friday periods correctly`() {
        val config = SchoolBellConfig(
            startHour = 7,
            startMinute = 0,
            periodDurationMinutes = 40,
            fridayStartHour = 7,
            fridayStartMinute = 0,
            fridayPeriodDurationMinutes = 35,
            fridayTotalPeriods = 6,
            fridayBreaks = listOf(
                BreakConfig(afterPeriod = 3, durationMinutes = 15, name = "Istirahat Jumat")
            )
        )

        assertEquals(6, config.getTotalPeriodsForDay(5))

        // Friday JP 1: 07:00 (420m) to 07:35 (455m)
        val (friP1Start, friP1End) = config.getPeriodTimeRangeForDay(5, 1)
        assertEquals(420, friP1Start)
        assertEquals(455, friP1End)

        // Friday JP 4: starts after Istirahat Jumat (15m) -> 420 + 3*35 + 15 = 540 (09:00)
        val (friP4Start, friP4End) = config.getPeriodTimeRangeForDay(5, 4)
        assertEquals(540, friP4Start)
        assertEquals(575, friP4End)
        assertEquals("09:00", SchoolBellConfig.formatMinutes(friP4Start))
        assertEquals("09:35", SchoolBellConfig.formatMinutes(friP4End))
    }

    @Test
    fun `day name helper returns correct Indonesian day names`() {
        assertEquals("Senin", SchoolBellConfig.getDayName(1))
        assertEquals("Selasa", SchoolBellConfig.getDayName(2))
        assertEquals("Rabu", SchoolBellConfig.getDayName(3))
        assertEquals("Kamis", SchoolBellConfig.getDayName(4))
        assertEquals("Jumat", SchoolBellConfig.getDayName(5))
        assertEquals("Sabtu", SchoolBellConfig.getDayName(6))
        assertEquals("Minggu", SchoolBellConfig.getDayName(7))
    }
}
