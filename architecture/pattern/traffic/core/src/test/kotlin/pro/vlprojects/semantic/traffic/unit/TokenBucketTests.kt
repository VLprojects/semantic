package pro.vlprojects.semantic.traffic.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.traffic.RateLimitResponse
import pro.vlprojects.semantic.traffic.TokenBucket
import pro.vlprojects.semantic.traffic.TokenBucketState
import java.time.Duration
import java.time.Instant

class TokenBucketTests {

    private val moment = Instant.parse("2026-11-10T00:00:00Z")
    private val bucket = TokenBucket.of(permitLimit = 3, window = Duration.ofSeconds(3))

    @Test
    fun `a new bucket is full and takes the permits from it`() {
        val expected = RateLimitResponse.acquired(remaining = 1, resetAfter = Duration.ofSeconds(2))

        val result = bucket.tryAcquire(null, 2, moment)

        assertEquals(TokenBucketState.of(1, moment), result.state)
        assertEquals(expected, result.response)
    }

    @Test
    fun `an empty bucket rejects until a token is refilled`() {
        val empty = TokenBucketState.of(0, moment)

        val result = bucket.tryAcquire(empty, 1, moment.plusMillis(400))

        assertFalse(result.response.isAcquired)
        assertEquals(Duration.ofMillis(600), result.response.retryAfter)
        assertEquals(Duration.ofMillis(2600), result.response.resetAfter)
        assertEquals(empty, result.state)
    }

    @Test
    fun `a token comes back each interval and the rest of the interval is kept`() {
        val empty = TokenBucketState.of(0, moment)

        val result = bucket.tryAcquire(empty, 1, moment.plusMillis(1500))

        assertEquals(TokenBucketState.of(0, moment.plusSeconds(1)), result.state)
        assertEquals(Duration.ofMillis(2500), result.response.resetAfter)
    }

    @Test
    fun `a bucket never holds more tokens than the limit`() {
        val result = bucket.tryAcquire(TokenBucketState.of(1, moment), 1, moment.plusSeconds(60))

        assertEquals(TokenBucketState.of(2, moment.plusSeconds(60)), result.state)
    }

    @Test
    fun `a clock behind the last refill adds no tokens`() {
        val result = bucket.tryAcquire(TokenBucketState.of(1, moment), 1, moment.minusSeconds(5))

        assertEquals(TokenBucketState.of(0, moment), result.state)
        assertEquals(Duration.ofSeconds(3), result.response.resetAfter)
    }

    @Test
    fun `a window shorter than one nanosecond per permit is rejected`() {
        val window = Duration.ofNanos(9)

        assertThrows<IllegalArgumentException> { TokenBucket.of(10, window) }
    }
}
