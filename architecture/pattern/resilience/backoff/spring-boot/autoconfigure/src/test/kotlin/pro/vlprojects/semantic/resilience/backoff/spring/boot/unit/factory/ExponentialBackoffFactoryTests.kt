package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.resilience.backoff.Backoff
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

    @Test
    fun `without a jitter the pause is exact`() {
        val backoff = factory.create(ExponentialBackoffFactory.Config())

        assertEquals(setOf(Duration.ofSeconds(1)), sampleDelays(backoff))
    }

    @Test
    fun `a jitter shortens the pause at random`() {
        val backoff = factory.create(ExponentialBackoffFactory.Config(jitter = 0.5))
        val delays = sampleDelays(backoff)

        assertTrue(delays.size > 1)
        assertTrue(delays.all { delay -> delay <= Duration.ofSeconds(1) })
    }

    private fun sampleDelays(backoff: Backoff) = (1..200)
        .map { backoff.calculate(1) }
        .toSet()
}
