package pro.vlprojects.semantic.events

import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration

public class ExponentialRetryPolicy(
    private val initialDelay: Duration,
    private val maxDelay: Duration,
    private val maxAttempts: Int,
    private val multiplier: Double = 2.0,
    private val jitter: Double = 0.0,
    private val random: Random = Random.Default,
) : RetryPolicy {

    init {
        require(initialDelay.isPositive()) { "The initial delay must be positive: $initialDelay" }
        require(maxDelay >= initialDelay) { "The max delay $maxDelay is less than the initial one $initialDelay" }
        require(maxAttempts > 0) { "The number of attempts must be positive: $maxAttempts" }
        require(multiplier >= 1.0) { "The multiplier must not be less than 1: $multiplier" }
        require(jitter in 0.0..1.0) { "The jitter must be within 0..1: $jitter" }
    }

    override fun delayAfter(attempt: Int): Duration? = attempt
        .takeIf { it < maxAttempts }
        ?.let(::grow)
        ?.let(::spread)

    private fun grow(attempt: Int) = multiplier
        .pow(attempt - 1)
        .coerceAtMost(maxDelay / initialDelay)
        .let { initialDelay * it }

    private fun spread(delay: Duration) = random
        .nextDouble()
        .times(jitter)
        .let { delay * (1 - it) }
}
