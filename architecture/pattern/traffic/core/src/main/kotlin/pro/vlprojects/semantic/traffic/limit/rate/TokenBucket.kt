package pro.vlprojects.semantic.traffic.limit.rate

import java.time.Duration
import java.time.Instant

public class TokenBucket private constructor(
    public val capacity: Int,
    public val period: Duration,
) : RateLimitStrategy {

    init {
        require(capacity >= 1) { "The capacity must be positive: $capacity" }
        require(period.toNanos() >= capacity) { "The period $period is too short for $capacity tokens" }
    }

    private val refillInterval = capacity
        .toLong()
        .let(period::dividedBy)

    override fun allow(state: RateLimitState?, tokens: Int, now: Instant, maxWait: Duration): RateLimitResult {
        require(tokens in 1..capacity) { "The tokens must be from 1 to $capacity: $tokens" }
        require(!maxWait.isNegative) { "The maximum wait must not be negative: $maxWait" }

        val bucket = refill(state, now)
        val delay = calculateRefillTime(bucket, tokens, now)

        return when {
            delay <= maxWait -> consume(bucket, tokens, delay, now)
            else -> reject(bucket, delay, now)
        }
    }

    private fun refill(state: RateLimitState?, now: Instant) = when (state) {
        is TokenBucketState -> refill(state, now)
        else -> TokenBucketState.of(capacity, now)
    }

    private fun refill(bucket: TokenBucketState, now: Instant): TokenBucketState {
        val added = calculateElapsed(bucket, now).dividedBy(refillInterval)

        if (added >= capacity - bucket.tokens) {
            return TokenBucketState.of(capacity, now)
        }

        val tokens = added.toInt() + bucket.tokens
        val refilledAt = refillInterval
            .multipliedBy(added)
            .let(bucket.refilledAt::plus)

        return TokenBucketState.of(tokens, refilledAt)
    }

    // Tokens taken ahead of the refill go below zero, so later calls wait behind this one.
    private fun consume(bucket: TokenBucketState, tokens: Int, delay: Duration, now: Instant): RateLimitResult {
        val consumed = TokenBucketState.of(bucket.tokens - tokens, bucket.refilledAt)
        val resetAfter = calculateRefillTime(consumed, capacity, now)

        return consumed
            .let(::measure)
            .let { measured -> RateLimitDecision.passed(measured, delay, resetAfter) }
            .let { decision -> RateLimitResult.of(consumed, decision) }
    }

    private fun reject(bucket: TokenBucketState, retryAfter: Duration, now: Instant): RateLimitResult {
        val resetAfter = calculateRefillTime(bucket, capacity, now)

        return bucket
            .let(::measure)
            .let { measured -> RateLimitDecision.rejected(measured, retryAfter, resetAfter) }
            .let { decision -> RateLimitResult.of(bucket, decision) }
    }

    private fun measure(bucket: TokenBucketState) = RateLimitDecision.Capacity.of(capacity, bucket.tokens)

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

        public fun of(capacity: Int, period: Duration): TokenBucket = TokenBucket(capacity, period)
    }
}
