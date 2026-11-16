package pro.vlprojects.semantic.resilience.circuitbreaker.unit.window

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallStatistics

class CallStatisticsTests {

    @Test
    fun `the rates are the shares of the total`() {
        val statistics = CallStatistics(total = 4, failed = 1, slow = 2)

        assertEquals(0.25, statistics.failureRate)
        assertEquals(0.5, statistics.slowRate)
    }

    @Test
    fun `the rates of no calls are zero`() {
        val statistics = CallStatistics(total = 0, failed = 0, slow = 0)

        assertEquals(0.0, statistics.failureRate)
        assertEquals(0.0, statistics.slowRate)
    }

    @Test
    fun `the failed calls cannot exceed the total`() {
        assertThrows<IllegalArgumentException> { CallStatistics(total = 1, failed = 2, slow = 0) }
    }

    @Test
    fun `the slow calls cannot exceed the total`() {
        assertThrows<IllegalArgumentException> { CallStatistics(total = 1, failed = 0, slow = 2) }
    }

    @Test
    fun `the total cannot be negative`() {
        assertThrows<IllegalArgumentException> { CallStatistics(total = -1, failed = 0, slow = 0) }
    }
}
