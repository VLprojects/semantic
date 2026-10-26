package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.delivery.outbox.FixedBackoff
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
}
