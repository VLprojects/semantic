package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
import org.springframework.core.env.SystemEnvironmentPropertySource
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffPropertiesRegistrar
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory
import java.time.Duration

private const val PREFIX = "semantic.resilience.backoff"

class BackoffPropertiesRegistrarTests {

    @Test
    fun `without settings the default backoff is exponential`() {
        val registry = registerBackoffs()

        assertEquals(Duration.ofSeconds(1), registry.default.calculate(1))
        assertEquals(Duration.ofSeconds(2), registry.default.calculate(2))
    }

    @Test
    fun `the default block takes the type of its key`() {
        val registry = registerBackoffs("$PREFIX.default.fixed.delay" to "3s")

        assertEquals(Duration.ofSeconds(3), registry.default.calculate(5))
    }

    @Test
    fun `an instance without a type takes the type and the fields of the default`() {
        val registry = registerBackoffs(
            "$PREFIX.default.fixed.delay" to "3s",
            "$PREFIX.instances.orders.jitter" to "0",
        )

        assertEquals(Duration.ofSeconds(3), registry["orders"].calculate(5))
    }

    @Test
    fun `without a jitter setting the pause is exact`() {
        val registry = registerBackoffs("$PREFIX.instances.orders.fixed.delay" to "1s")

        assertEquals(setOf(Duration.ofSeconds(1)), sampleDelays(registry["orders"]))
    }

    @Test
    fun `an instance inherits the jitter of the default`() {
        val registry = registerBackoffs(
            "$PREFIX.default.jitter" to "1.0",
            "$PREFIX.instances.orders.fixed.delay" to "1s",
        )

        assertTrue(sampleDelays(registry["orders"]).size > 1)
    }

    @Test
    fun `a zero jitter of an instance switches off the jitter of the default`() {
        val registry = registerBackoffs(
            "$PREFIX.default.jitter" to "1.0",
            "$PREFIX.instances.orders.jitter" to "0",
        )

        assertEquals(setOf(Duration.ofSeconds(1)), sampleDelays(registry["orders"]))
    }

    @Test
    fun `an instance takes another type with its own fields`() {
        val registry = registerBackoffs("$PREFIX.instances.payments.fixed.delay" to "5s")

        assertEquals(Duration.ofSeconds(5), registry["payments"].calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from a default of the same type`() {
        val registry = registerBackoffs(
            "$PREFIX.default.exponential.multiplier" to "3",
            "$PREFIX.instances.orders.exponential.initial-delay" to "200ms",
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
        assertEquals(Duration.ofMillis(600), registry["orders"].calculate(2))
    }

    @Test
    fun `a field of an instance beats the same field of the default`() {
        val registry = registerBackoffs(
            "$PREFIX.default.exponential.initial-delay" to "5s",
            "$PREFIX.instances.orders.exponential.initial-delay" to "200ms",
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
    }

    @Test
    fun `a field of an instance beats the field of the default written in another form`() {
        val registry = registerBackoffs(
            "$PREFIX.default.exponential.initial-delay" to "5s",
            "$PREFIX.instances.orders.exponential.initialDelay" to "200ms",
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
    }

    @Test
    fun `an instance of another type ignores the fields of the default`() {
        val registry = registerBackoffs(
            "$PREFIX.default.fixed.delay" to "5s",
            "$PREFIX.instances.orders.exponential.multiplier" to "3",
        )

        assertEquals(Duration.ofSeconds(1), registry["orders"].calculate(1))
    }

    @Test
    fun `an instance name with a hyphen gets its settings`() {
        val registry = registerBackoffs("$PREFIX.instances.order-events.fixed.delay" to "9s")

        assertEquals(Duration.ofSeconds(9), registry["order-events"].calculate(1))
    }

    @Test
    fun `an instance name with capitals gets its settings`() {
        val registry = registerBackoffs("$PREFIX.instances.Orders.fixed.delay" to "9s")

        assertEquals(Duration.ofSeconds(9), registry["Orders"].calculate(1))
    }

    @Test
    fun `an environment variable reaches the instance`() {
        val environment = createEnvironment(emptyMap())
        val variables = mapOf<String, Any>("SEMANTIC_RESILIENCE_BACKOFF_INSTANCES_PAYMENTS_FIXED_DELAY" to "5s")
        val source = SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, variables)

        environment.propertySources.replace(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, source)

        assertEquals(Duration.ofSeconds(5), registerBackoffs(environment)["payments"].calculate(1))
    }

    @Test
    fun `a placeholder in a field resolves`() {
        val registry = registerBackoffs(
            "pause" to "6s",
            "$PREFIX.instances.orders.fixed.delay" to "\${pause}",
        )

        assertEquals(Duration.ofSeconds(6), registry["orders"].calculate(1))
    }

    @Test
    fun `a factory of the application adds a type`() {
        val registry = registerBackoffs(
            "$PREFIX.instances.orders.once.delay" to "7s",
            factories = listOf(OnceBackoffFactory(), ExponentialBackoffFactory()),
        )

        assertEquals(Duration.ofSeconds(7), registry["orders"].calculate(1))
    }

    @Test
    fun `two factories of one type fail and name both`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs(factories = listOf(FixedBackoffFactory(), RivalFixedBackoffFactory()))
        }

        assertTrue(FixedBackoffFactory::class.java.name in failure.message.orEmpty())
        assertTrue("RivalFixedBackoffFactory" in failure.message.orEmpty())
    }

    @Test
    fun `an unknown type fails and lists the available ones`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.orders.linear.step" to "2s")
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("linear" in failure.message.orEmpty())
        assertTrue("fixed" in failure.message.orEmpty())
    }

    @Test
    fun `a misspelt jitter fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.orders.jiter" to "0.5")
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("jiter" in failure.message.orEmpty())
    }

    @Test
    fun `two types of one instance fail`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs(
                "$PREFIX.instances.orders.fixed.delay" to "5s",
                "$PREFIX.instances.orders.exponential.multiplier" to "3",
            )
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("Only one backoff type" in failure.message.orEmpty())
    }

    @Test
    fun `a value right on the type fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.orders.fixed" to "9s")
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("9s" in failure.message.orEmpty())
    }

    @Test
    fun `a field the type does not have fails with the names of the instance and the field`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.orders.exponential.delay" to "5s")
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("delay" in failure.message.orEmpty())
    }

    @Test
    fun `a field of a wrong format fails with the names of the instance and the field`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.orders.exponential.multiplier" to "a lot")
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("multiplier" in failure.message.orEmpty())
    }

    @Test
    fun `a value the backoff refuses fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.orders.jitter" to "1.5")
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("jitter" in failure.message.orEmpty())
    }

    @Test
    fun `an invalid default fails with the name of the default`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.default.exponential.multiplier" to "0.5")
        }

        assertTrue("'default'" in failure.message.orEmpty())
    }

    @Test
    fun `an instance named default fails as reserved`() {
        val failure = assertThrows<IllegalArgumentException> {
            registerBackoffs("$PREFIX.instances.default.jitter" to "0.5")
        }

        assertTrue("reserved" in failure.message.orEmpty())
    }

    private fun registerBackoffs(
        vararg properties: Pair<String, String>,
        factories: List<BackoffFactory<*>> = listOf(FixedBackoffFactory(), ExponentialBackoffFactory()),
    ) = properties
        .toMap()
        .let(::createEnvironment)
        .let { environment -> registerBackoffs(environment, factories) }

    private fun registerBackoffs(
        environment: StandardEnvironment,
        factories: List<BackoffFactory<*>> = listOf(FixedBackoffFactory(), ExponentialBackoffFactory()),
    ): BackoffRegistry {
        val registry = BackoffRegistry()

        BackoffPropertiesRegistrar(environment, factories).registerBackoffs(registry)

        return registry
    }

    private fun createEnvironment(properties: Map<String, String>) = StandardEnvironment().apply {
        this.propertySources.addFirst(MapPropertySource("test", properties))
    }

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
