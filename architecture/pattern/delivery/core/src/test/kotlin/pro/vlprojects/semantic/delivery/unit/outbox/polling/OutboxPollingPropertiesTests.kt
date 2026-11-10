package pro.vlprojects.semantic.delivery.unit.outbox.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.OutboxProperties
import pro.vlprojects.semantic.delivery.outbox.polling.ActiveIdlePollingProperties
import pro.vlprojects.semantic.delivery.outbox.polling.ActiveIdlePollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.AdaptivePollingProperties
import pro.vlprojects.semantic.delivery.outbox.polling.AdaptivePollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.FixedPollingProperties
import pro.vlprojects.semantic.delivery.outbox.polling.FixedPollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingProperties
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategyType
import java.time.Duration

class OutboxPollingPropertiesTests {

    @Test
    fun `the outbox polls by the active idle strategy by default`() {
        assertInstanceOf(ActiveIdlePollingStrategy::class.java, OutboxProperties().polling.buildStrategy())
    }

    @Test
    fun `the fixed strategy takes its interval`() {
        val strategy = OutboxPollingProperties(
            strategy = OutboxPollingStrategyType.FIXED,
            fixed = FixedPollingProperties(Duration.ofMillis(300)),
        ).buildStrategy()

        assertInstanceOf(FixedPollingStrategy::class.java, strategy)
        assertEquals(Duration.ofMillis(300), strategy.calculateAfter(0))
    }

    @Test
    fun `the active idle strategy takes its intervals`() {
        val strategy = OutboxPollingProperties(
            activeIdle = ActiveIdlePollingProperties(Duration.ofMillis(5), Duration.ofSeconds(3)),
        ).buildStrategy()

        assertEquals(Duration.ofMillis(5), strategy.calculateAfter(100))
        assertEquals(Duration.ofSeconds(3), strategy.calculateAfter(0))
    }

    @Test
    fun `the adaptive strategy takes its intervals`() {
        val strategy = OutboxPollingProperties(
            strategy = OutboxPollingStrategyType.ADAPTIVE,
            adaptive = AdaptivePollingProperties(Duration.ofMillis(50), Duration.ofSeconds(1)),
        ).buildStrategy()

        assertInstanceOf(AdaptivePollingStrategy::class.java, strategy)
        assertEquals(Duration.ofMillis(50), strategy.calculateAfter(100))
        assertEquals(Duration.ofSeconds(1), strategy.calculateAfter(0, Duration.ofMinutes(1)))
    }
}
