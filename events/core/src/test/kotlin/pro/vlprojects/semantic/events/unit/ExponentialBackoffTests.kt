package pro.vlprojects.semantic.events.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.events.ExponentialBackoff
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ExponentialBackoffTests {

    private val half = object : Random() {

        override fun nextBits(bitCount: Int) = 0

        override fun nextDouble() = 0.5
    }

    @Test
    fun `the delay grows by the multiplier with each attempt`() {
        val backoff = ExponentialBackoff(initialDelay = 1.seconds, maxDelay = 60.seconds)

        assertEquals(1.seconds, backoff.delayAfter(1))
        assertEquals(2.seconds, backoff.delayAfter(2))
        assertEquals(4.seconds, backoff.delayAfter(3))
        assertEquals(8.seconds, backoff.delayAfter(4))
    }

    @Test
    fun `the delay stops at the max one however many attempts were made`() {
        val backoff = ExponentialBackoff(initialDelay = 1.seconds, maxDelay = 5.seconds)

        assertEquals(5.seconds, backoff.delayAfter(4))
        assertEquals(5.seconds, backoff.delayAfter(2000))
    }

    @Test
    fun `the jitter shortens the delay by its share of a random number`() {
        val backoff = ExponentialBackoff(initialDelay = 4.seconds, maxDelay = 60.seconds, jitter = 0.5, random = half)

        assertEquals(3.seconds, backoff.delayAfter(1))
        assertEquals(6.seconds, backoff.delayAfter(2))
    }

    @Test
    fun `a backoff that cannot work is refused`() {
        assertThrows<IllegalArgumentException> { ExponentialBackoff(0.seconds, 60.seconds) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(2.seconds, 1.seconds) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(1.seconds, 60.seconds, multiplier = 0.5) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(1.seconds, 60.seconds, jitter = 1.5) }
    }
}
