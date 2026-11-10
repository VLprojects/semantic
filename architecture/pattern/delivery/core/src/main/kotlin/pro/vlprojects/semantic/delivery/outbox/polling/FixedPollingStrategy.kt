package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public class FixedPollingStrategy private constructor(private val interval: Duration) : OutboxPollingStrategy {

    init {
        require(interval.isPositive) { "The interval must be positive: $interval" }
    }

    override fun calculate(context: OutboxPollingContext): Duration = interval

    public companion object Factory {

        private val DEFAULT_INTERVAL = Duration.ofSeconds(1)

        public fun of(interval: Duration = DEFAULT_INTERVAL): FixedPollingStrategy = FixedPollingStrategy(interval)
    }
}
