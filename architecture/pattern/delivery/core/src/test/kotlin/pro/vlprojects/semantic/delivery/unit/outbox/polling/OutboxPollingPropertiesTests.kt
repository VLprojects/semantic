package pro.vlprojects.semantic.delivery.unit.outbox.polling

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.OutboxProperties
import pro.vlprojects.semantic.delivery.outbox.polling.ActiveIdlePollingStrategy

class OutboxPollingPropertiesTests {

    @Test
    fun `the outbox polls by the active idle strategy by default`() {
        assertInstanceOf(ActiveIdlePollingStrategy::class.java, OutboxProperties().polling.strategy)
    }
}
