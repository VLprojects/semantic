package pro.vlprojects.semantic.helper.unit.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.time.day
import pro.vlprojects.semantic.helper.time.days
import pro.vlprojects.semantic.helper.time.hour
import pro.vlprojects.semantic.helper.time.hours
import pro.vlprojects.semantic.helper.time.microsecond
import pro.vlprojects.semantic.helper.time.microseconds
import pro.vlprojects.semantic.helper.time.millisecond
import pro.vlprojects.semantic.helper.time.milliseconds
import pro.vlprojects.semantic.helper.time.minute
import pro.vlprojects.semantic.helper.time.minutes
import pro.vlprojects.semantic.helper.time.nanosecond
import pro.vlprojects.semantic.helper.time.nanoseconds
import pro.vlprojects.semantic.helper.time.second
import pro.vlprojects.semantic.helper.time.seconds

class DoubleHelperTests {

    @Test
    fun `a number builds a duration in each unit`() {
        assertEquals("2ns", 2.0.nanoseconds().toString())
        assertEquals("2us", 2.0.microseconds().toString())
        assertEquals("2ms", 2.0.milliseconds().toString())
        assertEquals("2s", 2.0.seconds().toString())
        assertEquals("2m", 2.0.minutes().toString())
        assertEquals("2h", 2.0.hours().toString())
        assertEquals("2d", 2.0.days().toString())
    }

    @Test
    fun `one builds a duration in the singular`() {
        assertEquals("1ns", 1.0.nanosecond().toString())
        assertEquals("1us", 1.0.microsecond().toString())
        assertEquals("1ms", 1.0.millisecond().toString())
        assertEquals("1s", 1.0.second().toString())
        assertEquals("1m", 1.0.minute().toString())
        assertEquals("1h", 1.0.hour().toString())
        assertEquals("1d", 1.0.day().toString())
    }

    @Test
    fun `a fraction is carried to the smaller units`() {
        assertEquals("1d 12h", 1.5.days().toString())
        assertEquals("1h 30m", 1.5.hours().toString())
    }
}
