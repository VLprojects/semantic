package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration
import kotlin.math.pow
import kotlin.random.Random

public class ExponentialBackoff(
    initialDelay: Duration,
    delayLimit: Duration,
    private val multiplier: Double = DEFAULT_MULTIPLIER,
    private val jitter: Double = 0.0,
    private val randomizer: Random = Random.Default,
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

    override fun calculate(attempt: Int): Duration = attempt
        .also(::checkAttemptIsPositive)
        .let(::grow)
        .let(::limit)
        .let(::spread)
        .let(Double::toLong)
        .let(Duration::ofNanos)

    private fun checkAttemptIsPositive(attempt: Int) = require(attempt >= 1) {
        "The attempt must be positive: $attempt"
    }

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
        ): ExponentialBackoff = ExponentialBackoff(initialDelay, delayLimit, multiplier)

        public fun ofJitter(
            initialDelay: Duration = DEFAULT_INITIAL_DELAY,
            delayLimit: Duration = DEFAULT_DELAY_LIMIT,
            multiplier: Double = DEFAULT_MULTIPLIER,
            jitter: Double = DEFAULT_JITTER,
            randomizer: Random = Random.Default,
        ): ExponentialBackoff = ExponentialBackoff(initialDelay, delayLimit, multiplier, jitter, randomizer)
    }
}
