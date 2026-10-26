package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.delivery.outbox.ExponentialBackoff
import java.time.Duration
import kotlin.random.Random

class ExponentialBackoffTests {

    private val half = object : Random() {

        override fun nextBits(bitCount: Int) = 0

        override fun nextDouble() = 0.5
    }

    @Test
    fun `the delay grows by the multiplier with each attempt`() {
        val backoff = ExponentialBackoff(initialDelay = Duration.ofSeconds(1), maxDelay = Duration.ofSeconds(60))

        assertEquals(Duration.ofSeconds(1), backoff.delayAfter(1))
        assertEquals(Duration.ofSeconds(2), backoff.delayAfter(2))
        assertEquals(Duration.ofSeconds(4), backoff.delayAfter(3))
        assertEquals(Duration.ofSeconds(8), backoff.delayAfter(4))
    }

    @Test
    fun `the delay stops at the max one however many attempts were made`() {
        val backoff = ExponentialBackoff(initialDelay = Duration.ofSeconds(1), maxDelay = Duration.ofSeconds(5))

        assertEquals(Duration.ofSeconds(5), backoff.delayAfter(4))
        assertEquals(Duration.ofSeconds(5), backoff.delayAfter(2000))
    }

    @Test
    fun `the jitter shortens the delay by its share of a random number`() {
        val backoff = ExponentialBackoff(
            initialDelay = Duration.ofSeconds(4),
            maxDelay = Duration.ofSeconds(60),
            jitter = 0.5,
            random = half,
        )

        assertEquals(Duration.ofSeconds(3), backoff.delayAfter(1))
        assertEquals(Duration.ofSeconds(6), backoff.delayAfter(2))
    }

    @Test
    fun `a backoff that cannot work is refused`() {
        val second = Duration.ofSeconds(1)
        val minute = Duration.ofMinutes(1)

        assertThrows<IllegalArgumentException> { ExponentialBackoff(Duration.ZERO, minute) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(minute, second) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(second, minute, multiplier = 0.5) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(second, minute, jitter = 1.5) }
    }
}
