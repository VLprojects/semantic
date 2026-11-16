package pro.vlprojects.semantic.delivery.unit.outbox.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.polling.AdaptivePollingStrategy
import java.time.Duration

class AdaptivePollingStrategyTests {

    private val strategy = AdaptivePollingStrategy.of(Duration.ofMillis(100), Duration.ofSeconds(10))

    @Test
    fun `a full batch halves the delay`() {
        assertEquals(Duration.ofSeconds(1), strategy.calculateDurationAfterBatch(100, Duration.ofSeconds(2)))
    }

    @Test
    fun `a batch of a quarter doubles the delay`() {
        assertEquals(Duration.ofSeconds(4), strategy.calculateDurationAfterBatch(25, Duration.ofSeconds(2)))
    }

    @Test
    fun `an empty batch doubles the delay`() {
        assertEquals(Duration.ofSeconds(4), strategy.calculateDurationAfterBatch(0, Duration.ofSeconds(2)))
    }

    @Test
    fun `a batch between a quarter and full keeps the delay`() {
        assertEquals(Duration.ofSeconds(2), strategy.calculateDurationAfterBatch(26, Duration.ofSeconds(2)))
    }

    @Test
    fun `the delay does not fall below the minimum interval`() {
        assertEquals(Duration.ofMillis(100), strategy.calculateDurationAfterBatch(100, Duration.ofMillis(150)))
    }

    @Test
    fun `the delay does not grow above the maximum interval`() {
        assertEquals(Duration.ofSeconds(10), strategy.calculateDurationAfterBatch(0, Duration.ofSeconds(8)))
    }

    @Test
    fun `the first batch starts from the minimum interval`() {
        assertEquals(Duration.ofMillis(200), strategy.calculateDurationAfterBatch(0))
    }

    @Test
    fun `a previous delay above the maximum interval is limited first`() {
        assertEquals(Duration.ofSeconds(5), strategy.calculateDurationAfterBatch(100, Duration.ofDays(365)))
    }

    @Test
    fun `the default intervals are from a hundred milliseconds to ten seconds`() {
        val defaults = AdaptivePollingStrategy.of()

        assertEquals(Duration.ofMillis(100), defaults.calculateDurationAfterBatch(100))
        assertEquals(Duration.ofSeconds(10), defaults.calculateDurationAfterBatch(0, Duration.ofMinutes(1)))
    }

    @Test
    fun `the multiplier sets the step of the delay`() {
        val strategy = AdaptivePollingStrategy.of(multiplier = 1.5)

        assertEquals(Duration.ofSeconds(3), strategy.calculateDurationAfterBatch(0, Duration.ofSeconds(2)))
        assertEquals(Duration.ofSeconds(2), strategy.calculateDurationAfterBatch(100, Duration.ofSeconds(3)))
    }

    @Test
    fun `the multiplier must not be less than one`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdaptivePollingStrategy.of(multiplier = 0.5)
        }
    }

    @Test
    fun `the minimum interval must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdaptivePollingStrategy.of(minInterval = Duration.ZERO)
        }
    }

    @Test
    fun `the maximum interval must not be less than the minimum interval`() {
        assertThrows(IllegalArgumentException::class.java) {
            AdaptivePollingStrategy.of(Duration.ofSeconds(2), Duration.ofSeconds(1))
        }
    }
}
