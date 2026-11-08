package pro.vlprojects.semantic.resilience.circuitbreaker.unit.window

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.FAILURE
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.SLOW_SUCCESS
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallOutcome.SUCCESS
import pro.vlprojects.semantic.resilience.circuitbreaker.window.CallStatistics
import pro.vlprojects.semantic.resilience.circuitbreaker.window.TimeWindow
import java.time.Duration

class TimeWindowTests {

    private val second = Duration.ofSeconds(1)
    private val clock = AdjustableClock()
    private val window = TimeWindow(Duration.ofSeconds(3), second, clock)

    @Test
    fun `calls of one bucket add up`() {
        window.record(FAILURE)

        assertEquals(CallStatistics(total = 2, failed = 1, slow = 1), window.record(SLOW_SUCCESS))
    }

    @Test
    fun `calls of the buckets within the span add up`() {
        window.record(FAILURE)
        clock.move(Duration.ofSeconds(2))

        assertEquals(CallStatistics(total = 2, failed = 1, slow = 0), window.record(SUCCESS))
    }

    @Test
    fun `a bucket expires when the span has passed`() {
        window.record(FAILURE)
        clock.move(second)
        window.record(SUCCESS)
        clock.move(Duration.ofSeconds(2))

        assertEquals(CallStatistics(total = 1, failed = 0, slow = 0), window.summarize())
    }

    @Test
    fun `a jump far beyond the span leaves nothing`() {
        window.record(FAILURE)
        clock.move(Duration.ofDays(365))

        assertEquals(CallStatistics(total = 0, failed = 0, slow = 0), window.summarize())
    }

    @Test
    fun `the calls after a jump go to a fresh bucket`() {
        window.record(FAILURE)
        clock.move(Duration.ofDays(365))
        window.record(SUCCESS)
        clock.move(second)

        assertEquals(CallStatistics(total = 2, failed = 0, slow = 0), window.record(SUCCESS))
    }

    @Test
    fun `a clock moved back keeps the calls`() {
        window.record(FAILURE)
        clock.move(second.negated())

        assertEquals(CallStatistics(total = 2, failed = 1, slow = 0), window.record(SUCCESS))
    }

    @Test
    fun `buckets expire again after a clock moved back`() {
        window.record(FAILURE)
        clock.move(Duration.ofDays(-1))
        clock.move(Duration.ofSeconds(3))

        assertEquals(CallStatistics(total = 0, failed = 0, slow = 0), window.summarize())
    }

    @Test
    fun `clearing forgets every call`() {
        window.record(FAILURE)

        window.clear()
        window.record(SUCCESS)

        assertEquals(CallStatistics(total = 1, failed = 0, slow = 0), window.summarize())
    }

    @Test
    fun `the span must be a multiple of the bucket`() {
        assertThrows<IllegalArgumentException> { TimeWindow(Duration.ofMillis(1500), second, clock) }
    }

    @Test
    fun `the span must hold more than one bucket`() {
        assertThrows<IllegalArgumentException> { TimeWindow(second, second, clock) }
    }

    @Test
    fun `the span must not hold too many buckets`() {
        assertThrows<IllegalArgumentException> { TimeWindow(Duration.ofDays(2), Duration.ofMillis(1), clock) }
    }

    @Test
    fun `the span must be positive`() {
        assertThrows<IllegalArgumentException> { TimeWindow(Duration.ZERO, second, clock) }
    }

    @Test
    fun `the bucket must be at least a millisecond`() {
        assertThrows<IllegalArgumentException> { TimeWindow(second, Duration.ofNanos(1), clock) }
    }

    @Test
    fun `the bucket must be whole milliseconds`() {
        assertThrows<IllegalArgumentException> {
            TimeWindow(Duration.ofNanos(3_000_000), Duration.ofNanos(1_500_000), clock)
        }
    }
}
