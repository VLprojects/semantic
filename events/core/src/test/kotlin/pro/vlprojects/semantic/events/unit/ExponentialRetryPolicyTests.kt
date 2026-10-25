package pro.vlprojects.semantic.events.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.events.ExponentialRetryPolicy
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ExponentialRetryPolicyTests {

    private val half = object : Random() {

        override fun nextBits(bitCount: Int) = 0

        override fun nextDouble() = 0.5
    }

    @Test
    fun `the delay grows by the multiplier with each attempt`() {
        val policy = ExponentialRetryPolicy(initialDelay = 1.seconds, maxDelay = 60.seconds, maxAttempts = 5)

        assertEquals(1.seconds, policy.delayAfter(1))
        assertEquals(2.seconds, policy.delayAfter(2))
        assertEquals(4.seconds, policy.delayAfter(3))
        assertEquals(8.seconds, policy.delayAfter(4))
    }

    @Test
    fun `the delay stops at the max one`() {
        val policy = ExponentialRetryPolicy(initialDelay = 1.seconds, maxDelay = 5.seconds, maxAttempts = 2000)

        assertEquals(5.seconds, policy.delayAfter(4))
        assertEquals(5.seconds, policy.delayAfter(1999))
    }

    @Test
    fun `the jitter shortens the delay by its share of a random number`() {
        val policy = ExponentialRetryPolicy(
            initialDelay = 4.seconds,
            maxDelay = 60.seconds,
            maxAttempts = 3,
            jitter = 0.5,
            random = half,
        )

        assertEquals(3.seconds, policy.delayAfter(1))
        assertEquals(6.seconds, policy.delayAfter(2))
    }

    @Test
    fun `no delay is given once the attempts run out`() {
        val policy = ExponentialRetryPolicy(initialDelay = 1.seconds, maxDelay = 60.seconds, maxAttempts = 3)

        assertNull(policy.delayAfter(3))
        assertNull(policy.delayAfter(4))
    }

    @Test
    fun `a policy that cannot work is refused`() {
        assertThrows<IllegalArgumentException> { ExponentialRetryPolicy(0.seconds, 60.seconds, 3) }
        assertThrows<IllegalArgumentException> { ExponentialRetryPolicy(2.seconds, 1.seconds, 3) }
        assertThrows<IllegalArgumentException> { ExponentialRetryPolicy(1.seconds, 60.seconds, 0) }
        assertThrows<IllegalArgumentException> { ExponentialRetryPolicy(1.seconds, 60.seconds, 3, multiplier = 0.5) }
        assertThrows<IllegalArgumentException> { ExponentialRetryPolicy(1.seconds, 60.seconds, 3, jitter = 1.5) }
    }
}
