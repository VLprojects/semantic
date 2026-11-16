package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffTypeRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory
import java.time.Duration

class BackoffTypeRegistryTests {

    @Test
    fun `a bean name without the suffix names the type`() {
        val types = BackoffTypeRegistry.of(mapOf("linearBackoffFactory" to OnceBackoffFactory()))

        assertNotNull(findType(types, "linear"))
        assertNull(findType(types, "linear-backoff-factory"))
    }

    @Test
    fun `a bean name without the suffix is the type itself`() {
        val types = BackoffTypeRegistry.of(mapOf("once" to OnceBackoffFactory()))

        assertNotNull(findType(types, "once"))
    }

    @Test
    fun `a camel case type is found by its name with a hyphen`() {
        val types = BackoffTypeRegistry.of(mapOf("onceOnlyBackoffFactory" to OnceBackoffFactory()))

        assertNotNull(findType(types, "once-only"))
        assertEquals(listOf("onceOnly"), types.names)
    }

    @Test
    fun `the type argument of the factory gives the config class`() {
        val types = BackoffTypeRegistry.of(mapOf("onceBackoffFactory" to OnceBackoffFactory()))

        assertEquals(OnceBackoffFactory.Config::class.java, findType(types, "once")?.configClass)
    }

    @Test
    fun `a bean name of several property names fails and names the bean`() {
        val failure = assertThrows<IllegalArgumentException> {
            BackoffTypeRegistry.of(mapOf("once.onlyBackoffFactory" to OnceBackoffFactory()))
        }

        assertTrue("once.onlyBackoffFactory" in failure.message.orEmpty())
    }

    @Test
    fun `a factory without a concrete config class fails and names the bean`() {
        val failure = assertThrows<IllegalArgumentException> {
            BackoffTypeRegistry.of(mapOf("anyBackoffFactory" to AnyConfigBackoffFactory<Duration>()))
        }

        assertTrue("anyBackoffFactory" in failure.message.orEmpty())
    }

    @Test
    fun `two beans of one type fail and name both`() {
        val failure = assertThrows<IllegalArgumentException> {
            BackoffTypeRegistry.of(
                mapOf(
                    "fixedBackoffFactory" to FixedBackoffFactory(),
                    "fixed" to FixedBackoffFactory(),
                ),
            )
        }

        assertTrue("[fixedBackoffFactory, fixed]" in failure.message.orEmpty())
    }

    private fun findType(types: BackoffTypeRegistry, name: String) = name
        .let(ConfigurationPropertyName::of)
        .let(types::find)

    private class OnceBackoffFactory : BackoffFactory<OnceBackoffFactory.Config> {

        override fun create(config: Config) = FixedBackoff(config.delay)

        data class Config(val delay: Duration = Duration.ofSeconds(4))
    }

    private class AnyConfigBackoffFactory<C : Any> : BackoffFactory<C> {

        override fun create(config: C) = FixedBackoff(Duration.ofSeconds(1))
    }
}
