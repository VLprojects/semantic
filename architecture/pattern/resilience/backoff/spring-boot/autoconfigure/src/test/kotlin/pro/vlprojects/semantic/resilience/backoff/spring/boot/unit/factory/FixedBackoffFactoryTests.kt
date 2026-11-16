package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory
import java.time.Duration

class FixedBackoffFactoryTests {

    private val factory = FixedBackoffFactory()

    @Test
    fun `the delay is a second by default`() {
        assertEquals(Duration.ofSeconds(1), factory.create(FixedBackoffFactory.Config()).calculate(3))
    }

    @Test
    fun `the delay comes from the config`() {
        val config = FixedBackoffFactory.Config(Duration.ofSeconds(5))

        assertEquals(Duration.ofSeconds(5), factory.create(config).calculate(3))
    }
}
