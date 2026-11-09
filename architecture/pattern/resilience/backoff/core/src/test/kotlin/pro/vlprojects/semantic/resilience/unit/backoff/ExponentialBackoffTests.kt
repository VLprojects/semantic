package pro.vlprojects.semantic.resilience.unit.backoff

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import java.time.Duration

class ExponentialBackoffTests {

    @Test
    fun `the delay grows by the multiplier with each attempt`() {
        val backoff = ExponentialBackoff(initialDelay = Duration.ofSeconds(1), delayLimit = Duration.ofSeconds(60))

        assertEquals(Duration.ofSeconds(1), backoff.calculate(1))
        assertEquals(Duration.ofSeconds(2), backoff.calculate(2))
        assertEquals(Duration.ofSeconds(4), backoff.calculate(3))
        assertEquals(Duration.ofSeconds(8), backoff.calculate(4))
    }

    @Test
    fun `the delay stops at the max one however many attempts were made`() {
        val backoff = ExponentialBackoff(initialDelay = Duration.ofSeconds(1), delayLimit = Duration.ofSeconds(5))

        assertEquals(Duration.ofSeconds(5), backoff.calculate(4))
        assertEquals(Duration.ofSeconds(5), backoff.calculate(2000))
    }

    @Test
    fun `a backoff that cannot work is refused`() {
        val second = Duration.ofSeconds(1)
        val minute = Duration.ofMinutes(1)

        assertThrows<IllegalArgumentException> { ExponentialBackoff(Duration.ZERO, minute) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(minute, second) }
        assertThrows<IllegalArgumentException> { ExponentialBackoff(second, minute, multiplier = 0.5) }
    }

    @Test
    fun `an attempt that is not positive is refused`() {
        val backoff = ExponentialBackoff(initialDelay = Duration.ofSeconds(1), delayLimit = Duration.ofSeconds(60))

        assertThrows<IllegalArgumentException> { backoff.calculate(0) }
    }

    @Test
    fun `by default the delay doubles from a second up to a minute`() {
        val backoff = ExponentialBackoff.of()

        assertEquals(Duration.ofSeconds(1), backoff.calculate(1))
        assertEquals(Duration.ofSeconds(4), backoff.calculate(3))
        assertEquals(Duration.ofMinutes(1), backoff.calculate(100))
    }

    @Test
    fun `a given multiplier replaces the default one`() {
        val backoff = ExponentialBackoff.of(Duration.ofSeconds(1), Duration.ofSeconds(60), multiplier = 3.0)

        assertEquals(Duration.ofSeconds(9), backoff.calculate(3))
    }
}
