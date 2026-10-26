package pro.vlprojects.semantic.helper.unit.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

class LongHelperTests {

    @Test
    fun `a number builds a duration of a fixed length`() {
        assertEquals(Duration.ofNanos(2), 2L.nanoseconds())
        assertEquals(Duration.of(2, ChronoUnit.MICROS), 2L.microseconds())
        assertEquals(Duration.ofMillis(2), 2L.milliseconds())
        assertEquals(Duration.ofSeconds(2), 2L.seconds())
        assertEquals(Duration.ofMinutes(2), 2L.minutes())
        assertEquals(Duration.ofHours(2), 2L.hours())
        assertEquals(Duration.ofDays(2), 2L.days())
        assertEquals(Duration.ofDays(14), 2L.weeks())
    }

    @Test
    fun `a number builds a period of the calendar`() {
        assertEquals(Period.ofMonths(2), 2L.months())
        assertEquals(Period.ofMonths(6), 2L.quarters())
        assertEquals(Period.ofYears(2), 2L.years())
        assertEquals(Period.ofYears(20), 2L.decades())
    }

    @Test
    fun `one builds a duration or a period in the singular`() {
        assertEquals(Duration.ofNanos(1), 1L.nanosecond())
        assertEquals(Duration.of(1, ChronoUnit.MICROS), 1L.microsecond())
        assertEquals(Duration.ofMillis(1), 1L.millisecond())
        assertEquals(Duration.ofSeconds(1), 1L.second())
        assertEquals(Duration.ofMinutes(1), 1L.minute())
        assertEquals(Duration.ofHours(1), 1L.hour())
        assertEquals(Duration.ofDays(1), 1L.day())
        assertEquals(Duration.ofDays(7), 1L.week())
        assertEquals(Period.ofMonths(1), 1L.month())
        assertEquals(Period.ofMonths(3), 1L.quarter())
        assertEquals(Period.ofYears(1), 1L.year())
        assertEquals(Period.ofYears(10), 1L.decade())
    }

    @Test
    fun `a number too large for the unit is refused`() {
        assertThrows<ArithmeticException> { Long.MAX_VALUE.weeks() }
        assertThrows<ArithmeticException> { Long.MAX_VALUE.months() }
    }
}
