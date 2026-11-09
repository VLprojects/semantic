package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffConfigurationService
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffDefaultProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory
import java.time.Duration

class BackoffConfigurationServiceTests {

    @Test
    fun `without settings the backoff is exponential`() {
        val backoff = createBackoff()

        assertEquals(Duration.ofSeconds(1), backoff.calculate(1))
        assertEquals(Duration.ofSeconds(2), backoff.calculate(2))
    }

    @Test
    fun `without a jitter setting the pause is exact`() {
        val backoff = createBackoff()

        assertEquals(setOf(Duration.ofSeconds(1)), sampleDelays(backoff))
    }

    @Test
    fun `an instance inherits the jitter of the default`() {
        val backoff = createBackoff(default = BackoffDefaultProperties(jitter = 1.0))

        assertTrue(sampleDelays(backoff).size > 1)
    }

    @Test
    fun `a zero jitter of an instance switches off the jitter of the default`() {
        val backoff = createBackoff(BackoffProperties(jitter = 0.0), BackoffDefaultProperties(jitter = 1.0))

        assertEquals(setOf(Duration.ofSeconds(1)), sampleDelays(backoff))
    }

    @Test
    fun `the jitter wraps any type`() {
        val backoff = createBackoff(BackoffProperties(type = "fixed", jitter = 1.0))

        assertTrue(sampleDelays(backoff).size > 1)
    }

    @Test
    fun `an instance takes another type with its own args`() {
        val backoff = createBackoff(BackoffProperties(type = "fixed", args = mapOf("delay" to "5s")))

        assertEquals(Duration.ofSeconds(5), backoff.calculate(3))
    }

    @Test
    fun `the args an instance leaves out come from a default of the same type`() {
        val backoff = createBackoff(
            BackoffProperties(args = mapOf("initial-delay" to "200ms")),
            BackoffDefaultProperties(args = mapOf("multiplier" to "3")),
        )

        assertEquals(Duration.ofMillis(200), backoff.calculate(1))
        assertEquals(Duration.ofMillis(600), backoff.calculate(2))
    }

    @Test
    fun `an arg of an instance beats the same arg of the default`() {
        val backoff = createBackoff(
            BackoffProperties(args = mapOf("initial-delay" to "200ms")),
            BackoffDefaultProperties(args = mapOf("initial-delay" to "5s")),
        )

        assertEquals(Duration.ofMillis(200), backoff.calculate(1))
    }

    @Test
    fun `an arg of an instance beats the arg of the default written in another form`() {
        val backoff = createBackoff(
            BackoffProperties(args = mapOf("initialDelay" to "200ms")),
            BackoffDefaultProperties(args = mapOf("initial-delay" to "5s")),
        )

        assertEquals(Duration.ofMillis(200), backoff.calculate(1))
    }

    @Test
    fun `an instance of another type ignores the args of the default`() {
        val backoff = createBackoff(
            BackoffProperties(type = "fixed"),
            BackoffDefaultProperties(args = mapOf("initial-delay" to "5s")),
        )

        assertEquals(Duration.ofSeconds(1), backoff.calculate(1))
    }

    @Test
    fun `a factory of the application adds a type`() {
        val service = BackoffConfigurationService(BackoffDefaultProperties(), listOf(OnceBackoffFactory()))

        val backoff = service.create("orders", BackoffProperties(type = "once"))

        assertEquals(Duration.ofSeconds(4), backoff.calculate(1))
    }

    @Test
    fun `two factories of one type fail and name both`() {
        val failure = assertThrows<IllegalArgumentException> {
            BackoffConfigurationService(
                BackoffDefaultProperties(),
                listOf(FixedBackoffFactory(), RivalFixedBackoffFactory()),
            )
        }

        assertTrue(FixedBackoffFactory::class.java.name in failure.message.orEmpty())
        assertTrue("RivalFixedBackoffFactory" in failure.message.orEmpty())
    }

    @Test
    fun `an unknown type fails and lists the available ones`() {
        val failure = assertThrows<IllegalArgumentException> { createBackoff(BackoffProperties(type = "linear")) }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("linear" in failure.message.orEmpty())
        assertTrue("fixed" in failure.message.orEmpty())
    }

    @Test
    fun `an arg the type does not have fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoff(BackoffProperties(args = mapOf("delay" to "5s")))
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("delay" in failure.message.orEmpty())
    }

    @Test
    fun `an arg of a wrong format fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoff(BackoffProperties(args = mapOf("multiplier" to "a lot")))
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("multiplier" in failure.message.orEmpty())
    }

    @Test
    fun `a value the backoff refuses fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> { createBackoff(BackoffProperties(jitter = 1.5)) }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("jitter" in failure.message.orEmpty())
    }

    private fun createBackoff(
        properties: BackoffProperties = BackoffProperties(),
        default: BackoffDefaultProperties = BackoffDefaultProperties(),
    ) = BackoffConfigurationService(default, listOf(FixedBackoffFactory(), ExponentialBackoffFactory()))
        .create("orders", properties)

    private fun sampleDelays(backoff: Backoff) = (1..200)
        .map { backoff.calculate(1) }
        .toSet()

    private class RivalFixedBackoffFactory : BackoffFactory<FixedBackoffFactory.Config> {

        override val type = "fixed"

        override val configClass = FixedBackoffFactory.Config::class.java

        override fun create(config: FixedBackoffFactory.Config) = FixedBackoff(config.delay)
    }

    private class OnceBackoffFactory : BackoffFactory<OnceBackoffFactory.Config> {

        override val type = "once"

        override val configClass = Config::class.java

        override fun create(config: Config) = FixedBackoff(config.delay)

        data class Config(val delay: Duration = Duration.ofSeconds(4))
    }
}
