package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffDefaultProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType
import java.time.Duration

class BackoffFactoryTests {

    @Test
    fun `without settings the backoff is exponential from a second to a minute`() {
        val backoff = createBackoff()

        assertEquals(Duration.ofSeconds(1), backoff.calculate(1))
        assertEquals(Duration.ofSeconds(2), backoff.calculate(2))
        assertEquals(Duration.ofMinutes(1), backoff.calculate(100))
    }

    @Test
    fun `without a jitter setting the pause is exact`() {
        val backoff = createBackoff()

        val delays = (1..200).map { backoff.calculate(1) }

        assertEquals(setOf(Duration.ofSeconds(1)), delays.toSet())
    }

    @Test
    fun `an instance inherits the jitter of the default`() {
        val backoff = createBackoff(default = BackoffDefaultProperties(jitter = 1.0))

        val delays = (1..200).map { backoff.calculate(1) }

        assertTrue(delays.distinct().size > 1)
    }

    @Test
    fun `a zero jitter of an instance switches off the jitter of the default`() {
        val backoff = createBackoff(BackoffProperties(jitter = 0.0), BackoffDefaultProperties(jitter = 1.0))

        val delays = (1..200).map { backoff.calculate(1) }

        assertEquals(setOf(Duration.ofSeconds(1)), delays.toSet())
    }

    @Test
    fun `an instance takes another type`() {
        val backoff = createBackoff(BackoffProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5)))

        assertEquals(Duration.ofSeconds(5), backoff.calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from the default`() {
        val backoff = createBackoff(
            BackoffProperties(initialDelay = Duration.ofMillis(200)),
            BackoffDefaultProperties(multiplier = 3.0),
        )

        assertEquals(Duration.ofMillis(200), backoff.calculate(1))
        assertEquals(Duration.ofMillis(600), backoff.calculate(2))
    }

    @Test
    fun `an instance of the other type inherits the fields of that type from the default`() {
        val backoff = createBackoff(
            BackoffProperties(type = BackoffType.FIXED),
            BackoffDefaultProperties(delay = Duration.ofSeconds(7)),
        )

        assertEquals(Duration.ofSeconds(7), backoff.calculate(1))
    }

    @Test
    fun `the jitter wraps a fixed backoff too`() {
        val backoff = createBackoff(
            BackoffProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5), jitter = 1.0),
        )

        val delays = (1..200).map { backoff.calculate(1) }

        assertTrue(delays.distinct().size > 1)
        assertTrue(delays.all { it <= Duration.ofSeconds(5) })
    }

    @Test
    fun `an exponential backoff rejects the delay field`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoff(BackoffProperties(delay = Duration.ofSeconds(5)))
        }

        assertTrue("delay" in failure.message.orEmpty())
    }

    @Test
    fun `a fixed backoff rejects the multiplier field`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoff(BackoffProperties(type = BackoffType.FIXED, multiplier = 3.0))
        }

        assertTrue("multiplier" in failure.message.orEmpty())
    }

    @Test
    fun `a fixed backoff rejects the initial delay field`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoff(BackoffProperties(type = BackoffType.FIXED, initialDelay = Duration.ofSeconds(1)))
        }

        assertTrue("initial-delay" in failure.message.orEmpty())
    }

    @Test
    fun `a fixed backoff rejects the delay limit field`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoff(BackoffProperties(type = BackoffType.FIXED, delayLimit = Duration.ofSeconds(1)))
        }

        assertTrue("delay-limit" in failure.message.orEmpty())
    }

    @Test
    fun `the default block may hold the fields of both types`() {
        val backoff = createBackoff(
            default = BackoffDefaultProperties(delay = Duration.ofSeconds(5), initialDelay = Duration.ofSeconds(2)),
        )

        assertEquals(Duration.ofSeconds(2), backoff.calculate(1))
    }

    @Test
    fun `an invalid value fails`() {
        val failure = assertThrows<IllegalArgumentException> { createBackoff(BackoffProperties(jitter = 1.5)) }

        assertTrue("jitter" in failure.message.orEmpty())
    }

    private fun createBackoff(
        properties: BackoffProperties = BackoffProperties(),
        default: BackoffDefaultProperties = BackoffDefaultProperties(),
    ) = BackoffFactory(default).create(properties)
}
