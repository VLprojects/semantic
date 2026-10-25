package pro.vlprojects.semantic.events.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.events.FixedRetryPolicy
import kotlin.time.Duration.Companion.seconds

class FixedRetryPolicyTests {

    @Test
    fun `the delay is the same after each attempt`() {
        val policy = FixedRetryPolicy(delay = 5.seconds, maxAttempts = 3)

        assertEquals(5.seconds, policy.delayAfter(1))
        assertEquals(5.seconds, policy.delayAfter(2))
    }

    @Test
    fun `no delay is given once the attempts run out`() {
        val policy = FixedRetryPolicy(delay = 5.seconds, maxAttempts = 3)

        assertNull(policy.delayAfter(3))
    }

    @Test
    fun `a policy that cannot work is refused`() {
        assertThrows<IllegalArgumentException> { FixedRetryPolicy(0.seconds, 3) }
        assertThrows<IllegalArgumentException> { FixedRetryPolicy(5.seconds, 0) }
    }
}
