package pro.vlprojects.semantic.traffic.unit.limit.rate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.traffic.limit.rate.RateLimitDecision
import pro.vlprojects.semantic.traffic.limit.rate.RateLimitDecision.Capacity
import pro.vlprojects.semantic.traffic.limit.rate.TokenBucket
import pro.vlprojects.semantic.traffic.limit.rate.TokenBucketState
import java.time.Duration
import java.time.Instant

class TokenBucketTests {

    private val moment = Instant.parse("2026-11-11T00:00:00Z")
    private val bucket = TokenBucket.of(capacity = 3, period = Duration.ofSeconds(3))

    @Test
    fun `a new bucket is full and takes the tokens from it`() {
        val expected = RateLimitDecision.passed(Capacity.of(3, 1), resetAfter = Duration.ofSeconds(2))

        val result = bucket.allow(null, 2, moment)

        assertEquals(TokenBucketState.of(1, moment), result.state)
        assertEquals(expected, result.decision)
    }

    @Test
    fun `an empty bucket rejects until a token is refilled`() {
        val empty = TokenBucketState.of(0, moment)
        val expected = RateLimitDecision.rejected(
            tokens = Capacity.of(3, 0),
            retryAfter = Duration.ofMillis(600),
            resetAfter = Duration.ofMillis(2600),
        )

        val result = bucket.allow(empty, 1, moment.plusMillis(400))

        assertEquals(empty, result.state)
        assertEquals(expected, result.decision)
    }

    @Test
    fun `a token comes back each interval and the rest of the interval is kept`() {
        val empty = TokenBucketState.of(0, moment)

        val result = bucket.allow(empty, 1, moment.plusMillis(1500))

        assertEquals(TokenBucketState.of(0, moment.plusSeconds(1)), result.state)
        assertEquals(Duration.ofMillis(2500), result.decision.resetAfter)
    }

    @Test
    fun `a bucket never holds more tokens than its capacity`() {
        val result = bucket.allow(TokenBucketState.of(1, moment), 1, moment.plusSeconds(60))

        assertEquals(TokenBucketState.of(2, moment.plusSeconds(60)), result.state)
    }

    @Test
    fun `a clock behind the last refill adds no tokens`() {
        val result = bucket.allow(TokenBucketState.of(1, moment), 1, moment.minusSeconds(5))

        assertEquals(TokenBucketState.of(0, moment), result.state)
        assertEquals(Duration.ofSeconds(3), result.decision.resetAfter)
    }

    @Test
    fun `more tokens than the capacity are rejected`() {
        assertThrows<IllegalArgumentException> { bucket.allow(null, 4, moment) }
    }

    @Test
    fun `a period shorter than one nanosecond a token is rejected`() {
        val period = Duration.ofNanos(9)

        assertThrows<IllegalArgumentException> { TokenBucket.of(10, period) }
    }
}
