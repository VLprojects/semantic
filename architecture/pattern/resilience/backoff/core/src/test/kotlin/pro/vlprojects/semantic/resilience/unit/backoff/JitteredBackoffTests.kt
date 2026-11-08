package pro.vlprojects.semantic.resilience.unit.backoff

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.JitteredBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import java.time.Duration
import kotlin.random.Random

class JitteredBackoffTests {

    private val half = object : Random() {

        override fun nextBits(bitCount: Int) = 0

        override fun nextDouble() = 0.5
    }

    private val fixed = FixedBackoff.ofSeconds(4)

    @Test
    fun `the jitter shortens a fixed delay by its share of a random number`() {
        val backoff = JitteredBackoff(fixed, jitter = 0.5, randomizer = half)

        assertEquals(Duration.ofSeconds(3), backoff.calculate(1))
    }

    @Test
    fun `the jitter shortens a growing delay at every attempt`() {
        val exponential = ExponentialBackoff(Duration.ofSeconds(4), Duration.ofSeconds(60))
        val backoff = JitteredBackoff(exponential, jitter = 0.5, randomizer = half)

        assertEquals(Duration.ofSeconds(3), backoff.calculate(1))
        assertEquals(Duration.ofSeconds(6), backoff.calculate(2))
    }

    @Test
    fun `no jitter leaves the delay as it is`() {
        val backoff = JitteredBackoff(fixed, jitter = 0.0, randomizer = half)

        assertEquals(Duration.ofSeconds(4), backoff.calculate(1))
    }

    @Test
    fun `a jitter outside its range is refused`() {
        assertThrows<IllegalArgumentException> { JitteredBackoff(fixed, jitter = -0.1) }
        assertThrows<IllegalArgumentException> { JitteredBackoff(fixed, jitter = 1.5) }
    }

    @Test
    fun `an attempt that is not positive is refused`() {
        val backoff = JitteredBackoff(fixed, jitter = 0.5)

        assertThrows<IllegalArgumentException> { backoff.calculate(0) }
    }

    @Test
    fun `by default the jitter takes away at most half of the delay`() {
        val backoff = JitteredBackoff.of(fixed, randomizer = half)

        assertEquals(Duration.ofSeconds(3), backoff.calculate(1))
    }

    @Test
    fun `a given jitter replaces the default one`() {
        val backoff = JitteredBackoff.of(fixed, jitter = 1.0, randomizer = half)

        assertEquals(Duration.ofSeconds(2), backoff.calculate(1))
    }

    @Test
    fun `any backoff can be jittered in place`() {
        val backoff = fixed.jittered(jitter = 1.0, randomizer = half)

        assertEquals(Duration.ofSeconds(2), backoff.calculate(1))
    }
}
