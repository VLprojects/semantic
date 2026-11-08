package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.Backoffs

class BackoffsTests {

    private val default = FixedBackoff.ofSeconds(1)
    private val payments = FixedBackoff.ofSeconds(5)
    private val backoffs = Backoffs(default, mapOf("payments" to payments))

    @Test
    fun `a backoff is found by its name`() {
        assertSame(payments, backoffs["payments"])
        assertSame(payments, backoffs.getOrDefault("payments"))
    }

    @Test
    fun `an unknown name fails and lists the known ones`() {
        val failure = assertThrows<NoSuchElementException> { backoffs["orders"] }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("payments" in failure.message.orEmpty())
    }

    @Test
    fun `an unknown name can take the default instead`() {
        assertSame(default, backoffs.getOrDefault("orders"))
    }
}
