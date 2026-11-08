package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

public class ExponentialBackoff(
    initialDelay: Duration,
    delayLimit: Duration,
    private val multiplier: Double = DEFAULT_MULTIPLIER,
    private val jitter: Double = 0.0,
    private val randomizer: Random = Random.Default,
    override val maxAttempts: Int? = null,
    override val deadline: Duration? = null,
) : Backoff {

    init {
        require(initialDelay.isPositive) { "The initial delay must be positive: $initialDelay" }
        require(delayLimit >= initialDelay) {
            "The delay limit $delayLimit is less than the initial delay $initialDelay"
        }
        require(multiplier >= 1.0) { "The multiplier must not be less than 1: $multiplier" }
        require(jitter in 0.0..1.0) { "The jitter must be within 0..1: $jitter" }
    }

    private val initialNanos = initialDelay.toNanos().toDouble()
    private val limitNanos = delayLimit.toNanos().toDouble()
    private val limits = BackoffLimits(maxAttempts, deadline)
    private val growingAttempts = limitNanos
        .div(initialNanos)
        .let(::ln)
        .div(ln(multiplier))
        .let(::ceil)
        .toInt()

    override fun calculateDelay(attempt: Int): Duration = attempt
        .also(::requirePositiveAttempt)
        .also(::verifyLimitsNotExceeded)
        .let(::grow)
        .let(::limit)
        .let(::spread)
        .let(Double::toLong)
        .let(Duration::ofNanos)

    private fun verifyLimitsNotExceeded(attempt: Int) = attempt
        .let(::sumDelays)
        .let { totalDelayNanos -> limits.verifyNotExceededAfter(attempt, totalDelayNanos) }

    // O(1) time and memory: a closed form of the geometric series up to the delay limit.
    private fun sumDelays(attempt: Int) = when (multiplier) {
        1.0 -> attempt * initialNanos
        else -> sumGrowingDelays(attempt) + sumLimitedDelays(attempt)
    }

    private fun sumGrowingDelays(attempt: Int) = attempt
        .coerceAtMost(growingAttempts)
        .let { multiplier.pow(it) - 1 }
        .div(multiplier - 1)
        .times(initialNanos)

    private fun sumLimitedDelays(attempt: Int) = attempt
        .minus(growingAttempts)
        .coerceAtLeast(0)
        .times(limitNanos)

    private fun grow(attempt: Int) = multiplier
        .pow(attempt - 1)
        .times(initialNanos)

    private fun limit(delay: Double) = delay.coerceAtMost(limitNanos)

    private fun spread(delay: Double) = randomizer
        .nextDouble()
        .times(jitter)
        .let { reduction -> delay * (1 - reduction) }

    public companion object Factory {

        private const val DEFAULT_MULTIPLIER = 2.0
        private const val DEFAULT_JITTER = 0.5
        private val DEFAULT_INITIAL_DELAY = Duration.ofSeconds(1)
        private val DEFAULT_DELAY_LIMIT = Duration.ofMinutes(1)

        public fun of(
            initialDelay: Duration = DEFAULT_INITIAL_DELAY,
            delayLimit: Duration = DEFAULT_DELAY_LIMIT,
            multiplier: Double = DEFAULT_MULTIPLIER,
            maxAttempts: Int? = null,
            deadline: Duration? = null,
        ): ExponentialBackoff = ExponentialBackoff(
            initialDelay,
            delayLimit,
            multiplier,
            maxAttempts = maxAttempts,
            deadline = deadline,
        )

        public fun withJitter(
            initialDelay: Duration = DEFAULT_INITIAL_DELAY,
            delayLimit: Duration = DEFAULT_DELAY_LIMIT,
            multiplier: Double = DEFAULT_MULTIPLIER,
            jitter: Double = DEFAULT_JITTER,
            randomizer: Random = Random.Default,
            maxAttempts: Int? = null,
            deadline: Duration? = null,
        ): ExponentialBackoff = ExponentialBackoff(
            initialDelay,
            delayLimit,
            multiplier,
            jitter,
            randomizer,
            maxAttempts,
            deadline,
        )
    }
}
