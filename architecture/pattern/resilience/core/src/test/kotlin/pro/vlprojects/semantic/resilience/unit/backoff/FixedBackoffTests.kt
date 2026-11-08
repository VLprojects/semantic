package pro.vlprojects.semantic.resilience.unit.backoff

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.BackoffLimitExceededException
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import java.time.Duration

class FixedBackoffTests {

    @Test
    fun `the delay is the same after each attempt`() {
        val backoff = FixedBackoff(Duration.ofSeconds(5))

        assertEquals(Duration.ofSeconds(5), backoff.calculateDelay(1))
        assertEquals(Duration.ofSeconds(5), backoff.calculateDelay(100))
    }

    @Test
    fun `a delay that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { FixedBackoff(Duration.ZERO) }
    }

    @Test
    fun `the factories build the delay from their units`() {
        assertEquals(Duration.ofSeconds(5), FixedBackoff.of(Duration.ofSeconds(5)).calculateDelay(1))
        assertEquals(Duration.ofMillis(250), FixedBackoff.ofMillis(250).calculateDelay(1))
        assertEquals(Duration.ofSeconds(5), FixedBackoff.ofSeconds(5).calculateDelay(1))
        assertEquals(Duration.ofMinutes(2), FixedBackoff.ofMinutes(2).calculateDelay(1))
    }

    @Test
    fun `the default delay is one second`() {
        assertEquals(Duration.ofSeconds(1), FixedBackoff.of().calculateDelay(1))
    }

    @Test
    fun `there is no delay after the last attempt`() {
        val backoff = FixedBackoff.ofSeconds(5, maxAttempts = 3)

        assertEquals(Duration.ofSeconds(5), backoff.calculateDelay(2))
        assertThrows<BackoffLimitExceededException> { backoff.calculateDelay(3) }
    }

    @Test
    fun `there is no delay once the delays pass the deadline`() {
        val backoff = FixedBackoff.ofSeconds(5, deadline = Duration.ofSeconds(20))

        assertEquals(Duration.ofSeconds(5), backoff.calculateDelay(4))
        assertThrows<BackoffLimitExceededException> { backoff.calculateDelay(5) }
    }

    @Test
    fun `the limit reached first stops the delays`() {
        val deadline = Duration.ofSeconds(20)

        assertThrows<BackoffLimitExceededException> {
            FixedBackoff.ofSeconds(5, maxAttempts = 2, deadline = deadline).calculateDelay(2)
        }
        assertThrows<BackoffLimitExceededException> {
            FixedBackoff.ofSeconds(5, maxAttempts = 9, deadline = deadline).calculateDelay(5)
        }
    }

    @Test
    fun `limits that are not positive are refused`() {
        assertThrows<IllegalArgumentException> { FixedBackoff.of(maxAttempts = 0) }
        assertThrows<IllegalArgumentException> { FixedBackoff.of(deadline = Duration.ZERO) }
    }

    @Test
    fun `an attempt that is not positive is refused`() {
        val backoff = FixedBackoff(Duration.ofSeconds(5))

        assertThrows<IllegalArgumentException> { backoff.calculateDelay(0) }
    }
}
