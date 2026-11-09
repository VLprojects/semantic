package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import java.time.Duration

class ExponentialBackoffFactoryTests {

    private val factory = ExponentialBackoffFactory()

    @Test
    fun `by default the backoff grows from a second to a minute`() {
        val backoff = factory.create(ExponentialBackoffFactory.Config())

        assertEquals(Duration.ofSeconds(1), backoff.calculate(1))
        assertEquals(Duration.ofSeconds(2), backoff.calculate(2))
        assertEquals(Duration.ofMinutes(1), backoff.calculate(100))
    }

    @Test
    fun `the fields come from the config`() {
        val config = ExponentialBackoffFactory.Config(Duration.ofMillis(200), Duration.ofSeconds(1), 3.0)
        val backoff = factory.create(config)

        assertEquals(Duration.ofMillis(600), backoff.calculate(2))
        assertEquals(Duration.ofSeconds(1), backoff.calculate(10))
    }
}
