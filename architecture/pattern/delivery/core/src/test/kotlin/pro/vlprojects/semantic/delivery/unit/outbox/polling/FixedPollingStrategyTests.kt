package pro.vlprojects.semantic.delivery.unit.outbox.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.polling.FixedPollingStrategy
import java.time.Duration

class FixedPollingStrategyTests {

    private val strategy = FixedPollingStrategy.of(Duration.ofMillis(500))

    @Test
    fun `a full batch waits the interval`() {
        assertEquals(Duration.ofMillis(500), strategy.calculateAfter(100))
    }

    @Test
    fun `an empty batch waits the interval`() {
        assertEquals(Duration.ofMillis(500), strategy.calculateAfter(0))
    }

    @Test
    fun `the default interval is one second`() {
        assertEquals(Duration.ofSeconds(1), FixedPollingStrategy.of().calculateAfter(0))
    }

    @Test
    fun `the interval must be positive`() {
        assertThrows(IllegalArgumentException::class.java) { FixedPollingStrategy.of(Duration.ZERO) }
    }
}
