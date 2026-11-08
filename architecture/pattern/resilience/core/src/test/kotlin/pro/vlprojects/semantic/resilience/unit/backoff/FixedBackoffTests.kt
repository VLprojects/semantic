package pro.vlprojects.semantic.resilience.unit.backoff

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import java.time.Duration

class FixedBackoffTests {

    @Test
    fun `the delay is the same after each attempt`() {
        val backoff = FixedBackoff(Duration.ofSeconds(5))

        assertEquals(Duration.ofSeconds(5), backoff.delayAfter(1))
        assertEquals(Duration.ofSeconds(5), backoff.delayAfter(100))
    }

    @Test
    fun `a delay that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { FixedBackoff(Duration.ZERO) }
    }

    @Test
    fun `the factories build the delay from their units`() {
        assertEquals(Duration.ofSeconds(5), FixedBackoff.of(Duration.ofSeconds(5)).delayAfter(1))
        assertEquals(Duration.ofMillis(250), FixedBackoff.ofMillis(250).delayAfter(1))
        assertEquals(Duration.ofSeconds(5), FixedBackoff.ofSeconds(5).delayAfter(1))
        assertEquals(Duration.ofMinutes(2), FixedBackoff.ofMinutes(2).delayAfter(1))
    }

    @Test
    fun `the default delay is one second`() {
        assertEquals(Duration.ofSeconds(1), FixedBackoff.of().delayAfter(1))
    }

    @Test
    fun `an attempt that is not positive is refused`() {
        val backoff = FixedBackoff(Duration.ofSeconds(5))

        assertThrows<IllegalArgumentException> { backoff.delayAfter(0) }
    }
}
