package pro.vlprojects.semantic.delivery.spring.boot.unit.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.spring.boot.polling.AdaptivePollingStrategyFactory
import java.time.Duration

class AdaptivePollingStrategyFactoryTests {

    private val factory = AdaptivePollingStrategyFactory()

    @Test
    fun `by default the delay doubles from a hundred milliseconds to ten seconds`() {
        val strategy = factory.create(AdaptivePollingStrategyFactory.Config())

        assertEquals(Duration.ofMillis(200), strategy.calculateDurationAfterBatch(0))
        assertEquals(Duration.ofSeconds(10), strategy.calculateDurationAfterBatch(0, Duration.ofSeconds(8)))
    }

    @Test
    fun `the fields come from the config`() {
        val config = AdaptivePollingStrategyFactory.Config(Duration.ofMillis(50), Duration.ofSeconds(1), 3.0)
        val strategy = factory.create(config)

        assertEquals(Duration.ofMillis(150), strategy.calculateDurationAfterBatch(0))
        assertEquals(Duration.ofSeconds(1), strategy.calculateDurationAfterBatch(0, Duration.ofMillis(900)))
    }
}
