package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration
import kotlin.math.pow
import kotlin.random.Random

public class ExponentialBackoff(
    private val initialDelay: Duration,
    private val maxDelay: Duration,
    private val multiplier: Double = 2.0,
    private val jitter: Double = 0.0,
    private val random: Random = Random.Default,
) : Backoff {

    init {
        require(initialDelay.isPositive) { "The initial delay must be positive: $initialDelay" }
        require(maxDelay >= initialDelay) { "The max delay $maxDelay is less than the initial one $initialDelay" }
        require(multiplier >= 1.0) { "The multiplier must not be less than 1: $multiplier" }
        require(jitter in 0.0..1.0) { "The jitter must be within 0..1: $jitter" }
    }

    private val maxGrowthFactor = maxDelay.toNanos() / initialDelay.toNanos().toDouble()

    override fun delayAfter(attempt: Int): Duration = attempt
        .also(::requirePositiveAttempt)
        .let(::calculateCappedGrowthFactor)
        .let(::shortenByRandomJitter)
        .let(::multiplyInitialDelayBy)

    private fun calculateCappedGrowthFactor(attempt: Int) = multiplier
        .pow(attempt - 1)
        .coerceAtMost(maxGrowthFactor)

    private fun shortenByRandomJitter(growthFactor: Double) = random
        .nextDouble()
        .times(jitter)
        .let { shortenedShare -> growthFactor * (1 - shortenedShare) }

    private fun multiplyInitialDelayBy(growthFactor: Double) = initialDelay
        .toNanos()
        .times(growthFactor)
        .toLong()
        .let(Duration::ofNanos)
}
