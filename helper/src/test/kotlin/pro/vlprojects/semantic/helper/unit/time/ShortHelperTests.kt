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

class ShortHelperTests {

    private val one: Short = 1
    private val two: Short = 2

    @Test
    fun `a number builds a duration in each unit`() {
        assertEquals("2ns", two.nanoseconds().toString())
        assertEquals("2us", two.microseconds().toString())
        assertEquals("2ms", two.milliseconds().toString())
        assertEquals("2s", two.seconds().toString())
        assertEquals("2m", two.minutes().toString())
        assertEquals("2h", two.hours().toString())
        assertEquals("2d", two.days().toString())
    }

    @Test
    fun `one builds a duration in the singular`() {
        assertEquals("1ns", one.nanosecond().toString())
        assertEquals("1us", one.microsecond().toString())
        assertEquals("1ms", one.millisecond().toString())
        assertEquals("1s", one.second().toString())
        assertEquals("1m", one.minute().toString())
        assertEquals("1h", one.hour().toString())
        assertEquals("1d", one.day().toString())
    }
}
