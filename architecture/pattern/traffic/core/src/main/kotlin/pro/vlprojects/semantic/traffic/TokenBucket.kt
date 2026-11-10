package pro.vlprojects.semantic.traffic

import java.time.Duration
import java.time.Instant

public class TokenBucket private constructor(
    override val permitLimit: Int,
    override val window: Duration,
) : RateLimitAlgorithm {

    init {
        require(permitLimit >= 1) { "The permit limit must be positive: $permitLimit" }
        require(window.toNanos() >= permitLimit) { "The window $window is too short for $permitLimit permits" }
    }

    private val refillInterval = permitLimit
        .toLong()
        .let(window::dividedBy)

    override fun tryAcquire(state: RateLimitState?, permits: Int, now: Instant): RateLimitResult {
        val bucket = refill(state, now)

        return when {
            bucket.tokens >= permits -> consume(bucket, permits, now)
            else -> reject(bucket, permits, now)
        }
    }

    private fun refill(state: RateLimitState?, now: Instant) = when (state) {
        is TokenBucketState -> refill(state, now)
        else -> TokenBucketState.of(permitLimit, now)
    }

    private fun refill(bucket: TokenBucketState, now: Instant): TokenBucketState {
        val added = calculateElapsed(bucket, now).dividedBy(refillInterval)

        if (added >= permitLimit - bucket.tokens) {
            return TokenBucketState.of(permitLimit, now)
        }

        val tokens = added.toInt() + bucket.tokens
        val refilledAt = refillInterval
            .multipliedBy(added)
            .let(bucket.refilledAt::plus)

        return TokenBucketState.of(tokens, refilledAt)
    }

    private fun consume(bucket: TokenBucketState, permits: Int, now: Instant): RateLimitResult {
        val consumed = TokenBucketState.of(bucket.tokens - permits, bucket.refilledAt)
        val resetAfter = calculateRefillTime(consumed, permitLimit, now)

        return RateLimitResponse
            .acquired(consumed.tokens, resetAfter)
            .let { response -> RateLimitResult.of(consumed, response) }
    }

    private fun reject(bucket: TokenBucketState, permits: Int, now: Instant) = RateLimitResponse
        .rejected(
            remaining = bucket.tokens,
            retryAfter = calculateRefillTime(bucket, permits, now),
            resetAfter = calculateRefillTime(bucket, permitLimit, now),
        )
        .let { response -> RateLimitResult.of(bucket, response) }

    private fun calculateRefillTime(bucket: TokenBucketState, tokens: Int, now: Instant): Duration {
        val elapsed = calculateElapsed(bucket, now)

        return (tokens - bucket.tokens)
            .toLong()
            .let(refillInterval::multipliedBy)
            .minus(elapsed)
            .coerceAtLeast(Duration.ZERO)
    }

    // A clock of another instance may be behind the time of the last refill.
    private fun calculateElapsed(bucket: TokenBucketState, now: Instant) = Duration
        .between(bucket.refilledAt, now)
        .coerceAtLeast(Duration.ZERO)

    public companion object Factory {

        public fun of(permitLimit: Int, window: Duration): TokenBucket = TokenBucket(permitLimit, window)
    }
}
