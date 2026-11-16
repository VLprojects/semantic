package pro.vlprojects.semantic.resilience.circuitbreaker.unit.window

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.FAILURE
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.SLOW_FAILURE
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.SLOW_SUCCESS
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.SUCCESS
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallStatistics
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CountWindow

class CountWindowTests {

    @Test
    fun `an empty window has no calls`() {
        assertEquals(CallStatistics(total = 0, failed = 0, slow = 0), CountWindow(3).summarize())
    }

    @Test
    fun `each outcome counts by its own columns`() {
        val window = CountWindow(4)

        window.record(SUCCESS)
        window.record(SLOW_SUCCESS)
        window.record(FAILURE)

        assertEquals(CallStatistics(total = 4, failed = 2, slow = 2), window.record(SLOW_FAILURE))
    }

    @Test
    fun `the newest call replaces the oldest one`() {
        val window = CountWindow(2)

        window.record(FAILURE)
        window.record(SUCCESS)

        assertEquals(CallStatistics(total = 2, failed = 0, slow = 0), window.record(SUCCESS))
    }

    @Test
    fun `the replacement wraps around the window`() {
        val window = CountWindow(2)

        repeat(5) { window.record(FAILURE) }
        window.record(SLOW_SUCCESS)

        assertEquals(CallStatistics(total = 2, failed = 1, slow = 1), window.summarize())
    }

    @Test
    fun `clearing forgets every call`() {
        val window = CountWindow(2)
        window.record(FAILURE)

        window.clear()
        window.record(SUCCESS)

        assertEquals(CallStatistics(total = 1, failed = 0, slow = 0), window.summarize())
    }

    @Test
    fun `the size must be positive`() {
        assertThrows<IllegalArgumentException> { CountWindow(0) }
    }
}
