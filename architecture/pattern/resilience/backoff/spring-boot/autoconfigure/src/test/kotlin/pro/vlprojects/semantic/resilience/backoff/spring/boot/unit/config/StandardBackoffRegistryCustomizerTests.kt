package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.StandardBackoffRegistryCustomizer
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffDefaultProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType
import java.time.Duration

class StandardBackoffRegistryCustomizerTests {

    @Test
    fun `without settings the default is exponential from a second to a minute`() {
        val default = createRegistry(default = BackoffDefaultProperties()).default

        assertEquals(Duration.ofSeconds(1), default.calculate(1))
        assertEquals(Duration.ofSeconds(2), default.calculate(2))
        assertEquals(Duration.ofMinutes(1), default.calculate(100))
    }

    @Test
    fun `without a jitter setting the pause is exact`() {
        val delays = (1..200).map { createRegistry().default.calculate(1) }

        assertEquals(setOf(Duration.ofSeconds(1)), delays.toSet())
    }

    @Test
    fun `an instance inherits the jitter of the default`() {
        val registry = createRegistry(
            default = BackoffDefaultProperties(jitter = 1.0),
            instances = mapOf("orders" to BackoffProperties()),
        )

        val delays = (1..200).map { registry["orders"].calculate(1) }

        assertTrue(delays.distinct().size > 1)
    }

    @Test
    fun `a zero jitter of an instance switches off the jitter of the default`() {
        val registry = createRegistry(
            default = BackoffDefaultProperties(jitter = 1.0),
            instances = mapOf("orders" to BackoffProperties(jitter = 0.0)),
        )

        val delays = (1..200).map { registry["orders"].calculate(1) }

        assertEquals(setOf(Duration.ofSeconds(1)), delays.toSet())
    }

    @Test
    fun `a backoff bean takes the place of the settings`() {
        val bean = FixedBackoff.ofSeconds(9)

        val registry = createRegistry(
            instances = mapOf("payments" to BackoffProperties(initialDelay = Duration.ofMillis(1))),
            backoffs = mapOf("payments" to bean),
        )

        assertSame(bean, registry["payments"])
    }

    @Test
    fun `an instance takes another type`() {
        val registry = createRegistry(
            default = BackoffDefaultProperties(),
            instances = mapOf(
                "payments" to BackoffProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5)),
            ),
        )

        assertEquals(Duration.ofSeconds(5), registry["payments"].calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from the default`() {
        val registry = createRegistry(
            default = BackoffDefaultProperties(multiplier = 3.0),
            instances = mapOf("orders" to BackoffProperties(initialDelay = Duration.ofMillis(200))),
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
        assertEquals(Duration.ofMillis(600), registry["orders"].calculate(2))
    }

    @Test
    fun `an instance of the other type inherits the fields of that type from the default`() {
        val registry = createRegistry(
            default = BackoffDefaultProperties(delay = Duration.ofSeconds(7)),
            instances = mapOf("payments" to BackoffProperties(type = BackoffType.FIXED)),
        )

        assertEquals(Duration.ofSeconds(7), registry["payments"].calculate(1))
    }

    @Test
    fun `the jitter wraps a fixed backoff too`() {
        val registry = createRegistry(
            instances = mapOf(
                "payments" to
                    BackoffProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5), jitter = 1.0),
            ),
        )

        val delays = (1..200).map { registry["payments"].calculate(1) }

        assertTrue(delays.distinct().size > 1)
        assertTrue(delays.all { it <= Duration.ofSeconds(5) })
    }

    @Test
    fun `a field the type of an instance does not use fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            createRegistry(instances = mapOf("payments" to BackoffProperties(delay = Duration.ofSeconds(5))))
        }

        assertTrue("payments" in failure.message.orEmpty())
        assertTrue("delay" in failure.message.orEmpty())
    }

    @Test
    fun `the default block may hold the fields of both types`() {
        val settings =
            BackoffDefaultProperties(delay = Duration.ofSeconds(5), initialDelay = Duration.ofSeconds(2))

        assertEquals(Duration.ofSeconds(2), createRegistry(default = settings).default.calculate(1))
    }

    @Test
    fun `an invalid value fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            createRegistry(instances = mapOf("orders" to BackoffProperties(jitter = 1.5)))
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("jitter" in failure.message.orEmpty())
    }

    @Test
    fun `an invalid default fails with its name`() {
        val failure = assertThrows<IllegalArgumentException> {
            createRegistry(default = BackoffDefaultProperties(multiplier = 0.5))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    @Test
    fun `an invalid field of the type the default does not use fails too`() {
        val failure = assertThrows<IllegalArgumentException> {
            createRegistry(default = BackoffDefaultProperties(delay = Duration.ofSeconds(-1)))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    @Test
    fun `an instance named default fails`() {
        val failure = assertThrows<IllegalStateException> {
            createRegistry(instances = mapOf("default" to BackoffProperties()))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    private fun createRegistry(
        default: BackoffDefaultProperties = BackoffDefaultProperties(),
        instances: Map<String, BackoffProperties> = emptyMap(),
        backoffs: Map<String, Backoff> = emptyMap(),
    ) = StandardBackoffRegistryCustomizer(BackoffConfigurationProperties(default, instances), backoffs)
        .let { customizer -> BackoffRegistry().also(customizer::customize) }
}
