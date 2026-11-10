package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public class ActiveIdlePollingStrategy private constructor(
    private val activeInterval: Duration,
    private val idleInterval: Duration,
) : OutboxPollingStrategy {

    init {
        require(!activeInterval.isNegative) { "The active interval must not be negative: $activeInterval" }
        require(idleInterval.isPositive) { "The idle interval must be positive: $idleInterval" }
        require(idleInterval >= activeInterval) {
            "The idle interval $idleInterval is less than the active interval $activeInterval"
        }
    }

    override fun calculate(context: OutboxPollingContext): Duration = when {
        context.isBatchFull() -> activeInterval
        else -> idleInterval
    }

    public companion object Factory {

        private val DEFAULT_ACTIVE_INTERVAL = Duration.ZERO
        private val DEFAULT_IDLE_INTERVAL = Duration.ofSeconds(1)

        public fun of(
            activeInterval: Duration = DEFAULT_ACTIVE_INTERVAL,
            idleInterval: Duration = DEFAULT_IDLE_INTERVAL,
        ): ActiveIdlePollingStrategy = ActiveIdlePollingStrategy(activeInterval, idleInterval)
    }
}
