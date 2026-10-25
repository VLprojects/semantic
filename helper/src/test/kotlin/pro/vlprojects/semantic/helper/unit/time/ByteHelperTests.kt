package pro.vlprojects.semantic.helper.unit.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.time.day
import pro.vlprojects.semantic.helper.time.days
import pro.vlprojects.semantic.helper.time.decade
import pro.vlprojects.semantic.helper.time.decades
import pro.vlprojects.semantic.helper.time.hour
import pro.vlprojects.semantic.helper.time.hours
import pro.vlprojects.semantic.helper.time.microsecond
import pro.vlprojects.semantic.helper.time.microseconds
import pro.vlprojects.semantic.helper.time.millisecond
import pro.vlprojects.semantic.helper.time.milliseconds
import pro.vlprojects.semantic.helper.time.minute
import pro.vlprojects.semantic.helper.time.minutes
import pro.vlprojects.semantic.helper.time.month
import pro.vlprojects.semantic.helper.time.months
import pro.vlprojects.semantic.helper.time.nanosecond
import pro.vlprojects.semantic.helper.time.nanoseconds
import pro.vlprojects.semantic.helper.time.quarter
import pro.vlprojects.semantic.helper.time.quarters
import pro.vlprojects.semantic.helper.time.second
import pro.vlprojects.semantic.helper.time.seconds
import pro.vlprojects.semantic.helper.time.week
import pro.vlprojects.semantic.helper.time.weeks
import pro.vlprojects.semantic.helper.time.year
import pro.vlprojects.semantic.helper.time.years
import java.time.Duration
import java.time.Period
import java.time.temporal.ChronoUnit

class ByteHelperTests {

    private val one: Byte = 1
    private val two: Byte = 2

    @Test
    fun `a number builds a duration of a fixed length`() {
        assertEquals(Duration.ofNanos(2), two.nanoseconds())
        assertEquals(Duration.of(2, ChronoUnit.MICROS), two.microseconds())
        assertEquals(Duration.ofMillis(2), two.milliseconds())
        assertEquals(Duration.ofSeconds(2), two.seconds())
        assertEquals(Duration.ofMinutes(2), two.minutes())
        assertEquals(Duration.ofHours(2), two.hours())
        assertEquals(Duration.ofDays(2), two.days())
        assertEquals(Duration.ofDays(14), two.weeks())
    }

    @Test
    fun `a number builds a period of the calendar`() {
        assertEquals(Period.ofMonths(2), two.months())
        assertEquals(Period.ofMonths(6), two.quarters())
        assertEquals(Period.ofYears(2), two.years())
        assertEquals(Period.ofYears(20), two.decades())
    }

    @Test
    fun `one builds a duration or a period in the singular`() {
        assertEquals(Duration.ofNanos(1), one.nanosecond())
        assertEquals(Duration.of(1, ChronoUnit.MICROS), one.microsecond())
        assertEquals(Duration.ofMillis(1), one.millisecond())
        assertEquals(Duration.ofSeconds(1), one.second())
        assertEquals(Duration.ofMinutes(1), one.minute())
        assertEquals(Duration.ofHours(1), one.hour())
        assertEquals(Duration.ofDays(1), one.day())
        assertEquals(Duration.ofDays(7), one.week())
        assertEquals(Period.ofMonths(1), one.month())
        assertEquals(Period.ofMonths(3), one.quarter())
        assertEquals(Period.ofYears(1), one.year())
        assertEquals(Period.ofYears(10), one.decade())
    }
}
