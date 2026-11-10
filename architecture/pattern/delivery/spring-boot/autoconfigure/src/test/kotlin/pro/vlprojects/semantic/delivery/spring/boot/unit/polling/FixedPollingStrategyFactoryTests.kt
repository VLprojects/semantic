package pro.vlprojects.semantic.delivery.spring.boot.unit.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.spring.boot.polling.FixedPollingStrategyFactory
import java.time.Duration

class FixedPollingStrategyFactoryTests {

    private val factory = FixedPollingStrategyFactory()

    @Test
    fun `by default the outbox is polled once a second`() {
        val strategy = factory.create(FixedPollingStrategyFactory.Config())

        assertEquals(Duration.ofSeconds(1), strategy.calculateDurationAfterBatch(100))
    }

    @Test
    fun `the interval comes from the config`() {
        val strategy = factory.create(FixedPollingStrategyFactory.Config(Duration.ofMillis(300)))

        assertEquals(Duration.ofMillis(300), strategy.calculateDurationAfterBatch(0))
    }
}
