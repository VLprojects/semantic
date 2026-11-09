package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffPropertiesMapper
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.DefaultBackoffProperties
import java.time.Duration

class BackoffPropertiesMapperTests {

    @Test
    fun `without settings the default is exponential from a second to a minute`() {
        val default = createRegistry(default = DefaultBackoffProperties(jitter = 0.0)).default

        assertEquals(Duration.ofSeconds(1), default.calculate(1))
        assertEquals(Duration.ofSeconds(2), default.calculate(2))
        assertEquals(Duration.ofMinutes(1), default.calculate(100))
    }

    @Test
    fun `without settings the pause is jittered by a half`() {
        val delays = (1..200).map { createRegistry().default.calculate(1) }

        assertTrue(delays.all { it in Duration.ofMillis(500)..Duration.ofSeconds(1) })
        assertTrue(delays.distinct().size > 1)
    }

    @Test
    fun `an instance takes another type`() {
        val registry = createRegistry(
            default = DefaultBackoffProperties(jitter = 0.0),
            instances = mapOf(
                "payments" to BackoffProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5)),
            ),
        )

        assertEquals(Duration.ofSeconds(5), registry["payments"].calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from the default`() {
        val registry = createRegistry(
            default = DefaultBackoffProperties(multiplier = 3.0, jitter = 0.0),
            instances = mapOf("orders" to BackoffProperties(initialDelay = Duration.ofMillis(200))),
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
        assertEquals(Duration.ofMillis(600), registry["orders"].calculate(2))
    }

    @Test
    fun `an instance of the other type inherits the fields of that type from the default`() {
        val registry = createRegistry(
            default = DefaultBackoffProperties(delay = Duration.ofSeconds(7), jitter = 0.0),
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
            DefaultBackoffProperties(delay = Duration.ofSeconds(5), initialDelay = Duration.ofSeconds(2), jitter = 0.0)

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
            createRegistry(default = DefaultBackoffProperties(multiplier = 0.5))
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
        default: DefaultBackoffProperties = DefaultBackoffProperties(),
        instances: Map<String, BackoffProperties> = emptyMap(),
    ) = BackoffConfigurationProperties(default, instances)
        .let(::BackoffPropertiesMapper)
        .asBackoffs()
        .let(::BackoffRegistry)
}
