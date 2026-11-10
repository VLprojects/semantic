package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public class AdaptivePollingStrategy private constructor(
    private val minInterval: Duration,
    private val maxInterval: Duration,
    private val multiplier: Double,
) : OutboxPollingStrategy {

    init {
        require(minInterval.isPositive) { "The minimum interval must be positive: $minInterval" }
        require(maxInterval >= minInterval) {
            "The maximum interval $maxInterval is less than the minimum interval $minInterval"
        }
        require(multiplier >= 1.0) { "The multiplier must not be less than 1: $multiplier" }
    }

    override fun calculateDelay(context: OutboxPollingContext): Duration = context
        .previousDelay
        .let(::limit)
        .toNanos()
        .let { nanos -> scale(nanos, context) }
        .let(Double::toLong)
        .let(Duration::ofNanos)
        .let(::limit)

    private fun scale(nanos: Long, context: OutboxPollingContext) = when {
        context.isBatchFull() -> nanos / multiplier
        isBatchQuarterFullAtMost(context) -> nanos * multiplier
        else -> nanos.toDouble()
    }

    private fun isBatchQuarterFullAtMost(context: OutboxPollingContext) =
        context.dispatched * QUARTER <= context.batchSize

    private fun limit(delay: Duration) = delay.coerceIn(minInterval, maxInterval)

    public companion object Factory {

        private const val QUARTER = 4
        private const val DEFAULT_MULTIPLIER = 2.0
        private val DEFAULT_MIN_INTERVAL = Duration.ofMillis(100)
        private val DEFAULT_MAX_INTERVAL = Duration.ofSeconds(10)

        public fun of(
            minInterval: Duration = DEFAULT_MIN_INTERVAL,
            maxInterval: Duration = DEFAULT_MAX_INTERVAL,
            multiplier: Double = DEFAULT_MULTIPLIER,
        ): AdaptivePollingStrategy = AdaptivePollingStrategy(minInterval, maxInterval, multiplier)
    }
}
