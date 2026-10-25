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

class LongHelperTests {

    @Test
    fun `a number builds a duration in each unit`() {
        assertEquals("2ns", 2L.nanoseconds().toString())
        assertEquals("2us", 2L.microseconds().toString())
        assertEquals("2ms", 2L.milliseconds().toString())
        assertEquals("2s", 2L.seconds().toString())
        assertEquals("2m", 2L.minutes().toString())
        assertEquals("2h", 2L.hours().toString())
        assertEquals("2d", 2L.days().toString())
    }

    @Test
    fun `one builds a duration in the singular`() {
        assertEquals("1ns", 1L.nanosecond().toString())
        assertEquals("1us", 1L.microsecond().toString())
        assertEquals("1ms", 1L.millisecond().toString())
        assertEquals("1s", 1L.second().toString())
        assertEquals("1m", 1L.minute().toString())
        assertEquals("1h", 1L.hour().toString())
        assertEquals("1d", 1L.day().toString())
    }
}
