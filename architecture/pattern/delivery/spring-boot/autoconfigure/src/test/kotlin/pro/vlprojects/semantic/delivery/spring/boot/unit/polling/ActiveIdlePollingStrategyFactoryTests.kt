package pro.vlprojects.semantic.delivery.spring.boot.unit.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.spring.boot.polling.ActiveIdlePollingStrategyFactory
import java.time.Duration

class ActiveIdlePollingStrategyFactoryTests {

    private val factory = ActiveIdlePollingStrategyFactory()

    @Test
    fun `by default a full batch goes on at once and an idle outbox is polled once a second`() {
        val strategy = factory.create(ActiveIdlePollingStrategyFactory.Config())

        assertEquals(Duration.ZERO, strategy.calculateDelayAfter(100))
        assertEquals(Duration.ofSeconds(1), strategy.calculateDelayAfter(0))
    }

    @Test
    fun `the intervals come from the config`() {
        val config = ActiveIdlePollingStrategyFactory.Config(Duration.ofMillis(5), Duration.ofSeconds(3))
        val strategy = factory.create(config)

        assertEquals(Duration.ofMillis(5), strategy.calculateDelayAfter(100))
        assertEquals(Duration.ofSeconds(3), strategy.calculateDelayAfter(0))
    }
}
