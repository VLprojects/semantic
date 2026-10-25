package pro.vlprojects.semantic.events.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.events.FixedBackoff
import kotlin.time.Duration.Companion.seconds

class FixedBackoffTests {

    @Test
    fun `the delay is the same after each attempt`() {
        val backoff = FixedBackoff(5.seconds)

        assertEquals(5.seconds, backoff.delayAfter(1))
        assertEquals(5.seconds, backoff.delayAfter(100))
    }

    @Test
    fun `a delay that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { FixedBackoff(0.seconds) }
    }
}
