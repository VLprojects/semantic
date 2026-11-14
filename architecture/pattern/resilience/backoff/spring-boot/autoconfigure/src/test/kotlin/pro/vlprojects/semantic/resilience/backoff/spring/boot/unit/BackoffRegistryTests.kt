package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME

class BackoffRegistryTests {

    private val default = FixedBackoff.ofSeconds(1)
    private val payments = FixedBackoff.ofSeconds(5)
    private val registry = BackoffRegistry().also { registry ->
        registry.register(DEFAULT_BACKOFF_NAME, default)
        registry.register("payments", payments)
    }

    @Test
    fun `a backoff is found by its name`() {
        assertSame(payments, registry["payments"])
        assertSame(payments, registry.getOrDefault("payments"))
    }

    @Test
    fun `the default is found by its name`() {
        assertSame(default, registry.default)
        assertSame(default, registry[DEFAULT_BACKOFF_NAME])
    }

    @Test
    fun `an unknown name fails and lists the known ones`() {
        val failure = assertThrows<NoSuchElementException> { registry["orders"] }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("payments" in failure.message.orEmpty())
    }

    @Test
    fun `an unknown name can take the default instead`() {
        assertSame(default, registry.getOrDefault("orders"))
    }

    @Test
    fun `the names list every backoff`() {
        assertEquals(setOf(DEFAULT_BACKOFF_NAME, "payments"), registry.names)
    }

    @Test
    fun `the names stay the same while the registry changes`() {
        val names = registry.names

        registry.register("orders", payments)

        assertFalse("orders" in names)
    }

    @Test
    fun `registering a free name adds it`() {
        registry.register("orders", payments)

        assertSame(payments, registry["orders"])
    }

    @Test
    fun `registering a taken name fails`() {
        val failure = assertThrows<IllegalStateException> { registry.register("payments", default) }

        assertTrue("payments" in failure.message.orEmpty())
    }

    @Test
    fun `replacing takes a taken name`() {
        registry.replace("payments", default)

        assertSame(default, registry["payments"])
    }

    @Test
    fun `replacing adds a free name`() {
        registry.replace("orders", payments)

        assertSame(payments, registry["orders"])
    }
}
