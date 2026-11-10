package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffPropertiesRegistrar
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffTypeRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory
import java.time.Duration

class BackoffPropertiesRegistrarTests {

    @Test
    fun `without settings the default backoff is exponential`() {
        val registry = registerBackoffs()

        assertEquals(Duration.ofSeconds(1), registry.default.calculate(1))
        assertEquals(Duration.ofSeconds(2), registry.default.calculate(2))
    }

    @Test
    fun `the default block takes the type of its key`() {
        val registry = registerBackoffs(default = mapOf("fixed.delay" to "3s"))

        assertEquals(Duration.ofSeconds(3), registry.default.calculate(5))
    }

    @Test
    fun `an instance without a type takes the type and the fields of the default`() {
        val registry = registerBackoffs(
            default = mapOf("fixed.delay" to "3s"),
            instances = mapOf("orders" to emptyMap()),
        )

        assertEquals(Duration.ofSeconds(3), registry["orders"].calculate(5))
    }

    @Test
    fun `an instance takes another type with its own fields`() {
        val registry = registerBackoffs(instances = mapOf("orders" to mapOf("fixed.delay" to "5s")))

        assertEquals(Duration.ofSeconds(5), registry["orders"].calculate(3))
    }

    @Test
    fun `a type with no fields takes the defaults of its config`() {
        val registry = registerBackoffs(instances = mapOf("orders" to mapOf("fixed" to "")))

        assertEquals(Duration.ofSeconds(1), registry["orders"].calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from a default of the same type`() {
        val registry = registerBackoffs(
            default = mapOf("exponential.initial-delay" to "200ms"),
            instances = mapOf("orders" to mapOf("exponential.multiplier" to "3")),
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
        assertEquals(Duration.ofMillis(600), registry["orders"].calculate(2))
    }

    @Test
    fun `a field of an instance beats the same field of the default written in another form`() {
        val registry = registerBackoffs(
            default = mapOf("exponential.initialDelay" to "5s"),
            instances = mapOf("orders" to mapOf("exponential.initial-delay" to "200ms")),
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
    }

    @Test
    fun `an instance of another type ignores the fields of the default`() {
        val registry = registerBackoffs(
            default = mapOf("exponential.initial-delay" to "5s"),
            instances = mapOf("orders" to mapOf("fixed" to "")),
        )

        assertEquals(Duration.ofSeconds(1), registry["orders"].calculate(1))
    }

    @Test
    fun `an instance inherits the jitter of a default of the same type`() {
        val registry = registerBackoffs(
            default = mapOf("exponential.jitter" to "0.5"),
            instances = mapOf("orders" to mapOf("exponential.initial-delay" to "1s")),
        )

        assertTrue(sampleDelays(registry["orders"]).size > 1)
    }

    @Test
    fun `a zero jitter of an instance switches off the jitter of the default`() {
        val registry = registerBackoffs(
            default = mapOf("exponential.jitter" to "0.5"),
            instances = mapOf("orders" to mapOf("exponential.jitter" to "0")),
        )

        assertEquals(setOf(Duration.ofSeconds(1)), sampleDelays(registry["orders"]))
    }

    @Test
    fun `a type key in another form finds its factory`() {
        val registry = registerBackoffs(
            instances = mapOf("orders" to mapOf("once-only.delay" to "7s")),
            factories = createFactories() + ("onceOnlyBackoffFactory" to OnceBackoffFactory()),
        )

        assertEquals(Duration.ofSeconds(7), registry["orders"].calculate(1))
    }

    @Test
    fun `an unknown type fails and lists the available ones`() {
        val failure = assertThrows<InvalidConfigurationPropertyValueException> {
            registerBackoffs(instances = mapOf("orders" to mapOf("linear.step" to "2s")))
        }

        assertTrue("instances[orders]" in failure.message.orEmpty())
        assertTrue("linear" in failure.message.orEmpty())
        assertTrue("[fixed, exponential]" in failure.message.orEmpty())
    }

    @Test
    fun `two types of one instance fail`() {
        val failure = assertThrows<InvalidConfigurationPropertyValueException> {
            registerBackoffs(
                instances = mapOf("orders" to mapOf("fixed.delay" to "5s", "exponential.initial-delay" to "1s")),
            )
        }

        assertTrue("instances[orders]" in failure.message.orEmpty())
        assertTrue("Only one backoff type" in failure.message.orEmpty())
    }

    @Test
    fun `a value right on the type fails with the name of the instance`() {
        val failure = assertThrows<BindException> {
            registerBackoffs(instances = mapOf("orders" to mapOf("fixed" to "9s")))
        }

        assertTrue("instances[orders].fixed" in failure.message.orEmpty())
    }

    @Test
    fun `a field the type does not have fails with the names of the instance and the field`() {
        val failure = assertThrows<BindException> {
            registerBackoffs(instances = mapOf("orders" to mapOf("exponential.delay" to "5s")))
        }

        assertTrue("instances[orders].exponential.delay" in failure.cause?.message.orEmpty())
    }

    @Test
    fun `a field of a wrong format fails with the names of the instance and the field`() {
        val failure = assertThrows<BindException> {
            registerBackoffs(instances = mapOf("orders" to mapOf("exponential.multiplier" to "abc")))
        }

        assertTrue("instances[orders].exponential.multiplier" in failure.message.orEmpty())
    }

    @Test
    fun `a value the backoff refuses fails with the name of the instance`() {
        val failure = assertThrows<InvalidConfigurationPropertyValueException> {
            registerBackoffs(instances = mapOf("orders" to mapOf("exponential.jitter" to "1.5")))
        }

        assertTrue("instances[orders]" in failure.message.orEmpty())
        assertTrue("jitter" in failure.message.orEmpty())
    }

    @Test
    fun `an invalid default fails with the name of the default`() {
        val failure = assertThrows<InvalidConfigurationPropertyValueException> {
            registerBackoffs(default = mapOf("exponential.multiplier" to "0.5"))
        }

        assertTrue("semantic.resilience.backoff.default" in failure.message.orEmpty())
    }

    @Test
    fun `an instance named default fails as reserved`() {
        val failure = assertThrows<InvalidConfigurationPropertyValueException> {
            registerBackoffs(instances = mapOf("default" to mapOf("fixed.delay" to "5s")))
        }

        assertTrue("reserved" in failure.message.orEmpty())
    }

    private fun registerBackoffs(
        default: Map<String, String> = emptyMap(),
        instances: Map<String, Map<String, String>> = emptyMap(),
        factories: Map<String, BackoffFactory<*>> = createFactories(),
    ): BackoffRegistry {
        val properties = BackoffProperties(default, instances)
        val types = BackoffTypeRegistry.of(factories)
        val registry = BackoffRegistry()

        BackoffPropertiesRegistrar(properties, types).registerBackoffs(registry)

        return registry
    }

    private fun createFactories() = mapOf(
        "fixedBackoffFactory" to FixedBackoffFactory(),
        "exponentialBackoffFactory" to ExponentialBackoffFactory(),
    )

    private fun sampleDelays(backoff: Backoff) = (1..200)
        .map { backoff.calculate(1) }
        .toSet()

    private class OnceBackoffFactory : BackoffFactory<OnceBackoffFactory.Config> {

        override fun create(config: Config) = FixedBackoff(config.delay)

        data class Config(val delay: Duration = Duration.ofSeconds(4))
    }
}
