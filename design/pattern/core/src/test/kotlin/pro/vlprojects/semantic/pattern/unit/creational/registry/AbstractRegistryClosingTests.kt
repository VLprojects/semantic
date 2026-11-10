package pro.vlprojects.semantic.pattern.unit.creational.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AbstractRegistryClosingTests {

    private val registry = ConnectionRegistry()

    @Test
    fun `a close closes every instance and takes it out`() {
        val orders = Connection()
        val payments = Connection()
        registry.register("orders", orders)
        registry.register("payments", payments)

        registry.close()

        assertTrue(orders.isClosed)
        assertTrue(payments.isClosed)
        assertTrue(registry.getAll().isEmpty())
    }

    @Test
    fun `a failure of one instance closes the others and keeps the rest suppressed`() {
        val first = IllegalStateException("orders")
        val second = IllegalStateException("payments")
        val audit = Connection()
        registry.register("orders", BrokenConnection(first))
        registry.register("payments", BrokenConnection(second))
        registry.register("audit", audit)

        val failure = assertThrows<IllegalStateException> { registry.close() }

        assertTrue(audit.isClosed)
        assertEquals(setOf(first, second), setOf(failure, *failure.suppressed))
        assertTrue(registry.getAll().isEmpty())
    }

    @Test
    fun `an instance that is not closeable is only taken out`() {
        val greetings = GreetingRegistry()
        greetings.register("morning", "Good morning")

        greetings.close()

        assertTrue(greetings.getAll().isEmpty())
    }

    @Test
    fun `a closed registry takes new instances`() {
        registry.close()
        val orders = Connection()

        registry.register("orders", orders)

        assertSame(orders, registry.resolve("orders"))
    }
}
