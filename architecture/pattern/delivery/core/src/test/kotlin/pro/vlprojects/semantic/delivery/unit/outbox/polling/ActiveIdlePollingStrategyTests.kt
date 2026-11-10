package pro.vlprojects.semantic.delivery.unit.outbox.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.polling.ActiveIdlePollingStrategy
import java.time.Duration

class ActiveIdlePollingStrategyTests {

    private val strategy = ActiveIdlePollingStrategy.of(Duration.ofMillis(10), Duration.ofSeconds(2))

    @Test
    fun `a full batch waits the active interval`() {
        assertEquals(Duration.ofMillis(10), strategy.calculateDurationAfterBatch(100))
    }

    @Test
    fun `a partial batch waits the idle interval`() {
        assertEquals(Duration.ofSeconds(2), strategy.calculateDurationAfterBatch(99))
    }

    @Test
    fun `an empty batch waits the idle interval`() {
        assertEquals(Duration.ofSeconds(2), strategy.calculateDurationAfterBatch(0))
    }

    @Test
    fun `a full batch goes on at once by default`() {
        assertEquals(Duration.ZERO, ActiveIdlePollingStrategy.of().calculateDurationAfterBatch(100))
    }

    @Test
    fun `an idle database is polled once a second by default`() {
        assertEquals(Duration.ofSeconds(1), ActiveIdlePollingStrategy.of().calculateDurationAfterBatch(0))
    }

    @Test
    fun `the active interval must not be negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActiveIdlePollingStrategy.of(activeInterval = Duration.ofMillis(-1))
        }
    }

    @Test
    fun `the idle interval must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActiveIdlePollingStrategy.of(idleInterval = Duration.ZERO)
        }
    }

    @Test
    fun `the idle interval must not be less than the active interval`() {
        assertThrows(IllegalArgumentException::class.java) {
            ActiveIdlePollingStrategy.of(Duration.ofSeconds(2), Duration.ofSeconds(1))
        }
    }
}
