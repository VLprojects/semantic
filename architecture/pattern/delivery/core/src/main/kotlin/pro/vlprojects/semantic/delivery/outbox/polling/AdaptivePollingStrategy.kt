package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public class AdaptivePollingStrategy private constructor(
    private val minInterval: Duration,
    private val maxInterval: Duration,
) : OutboxPollingStrategy {

    init {
        require(minInterval.isPositive) { "The minimum interval must be positive: $minInterval" }
        require(maxInterval >= minInterval) {
            "The maximum interval $maxInterval is less than the minimum interval $minInterval"
        }
    }

    override fun calculate(context: OutboxPollingContext): Duration = context.previousDelay
        .let(::limit)
        .let { delay -> scale(delay, context) }
        .let(::limit)

    private fun scale(delay: Duration, context: OutboxPollingContext) = when {
        context.isBatchFull() -> delay.dividedBy(FACTOR)
        isBatchQuarterFullAtMost(context) -> delay.multipliedBy(FACTOR)
        else -> delay
    }

    private fun isBatchQuarterFullAtMost(context: OutboxPollingContext) =
        context.dispatched * QUARTER <= context.batchSize

    private fun limit(delay: Duration) = delay.coerceIn(minInterval, maxInterval)

    public companion object Factory {

        private const val FACTOR = 2L
        private const val QUARTER = 4
        private val DEFAULT_MIN_INTERVAL = Duration.ofMillis(100)
        private val DEFAULT_MAX_INTERVAL = Duration.ofSeconds(10)

        public fun of(
            minInterval: Duration = DEFAULT_MIN_INTERVAL,
            maxInterval: Duration = DEFAULT_MAX_INTERVAL,
        ): AdaptivePollingStrategy = AdaptivePollingStrategy(minInterval, maxInterval)
    }
}
