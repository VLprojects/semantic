package pro.vlprojects.semantic.delivery.outbox.polling

public data class OutboxPollingProperties(
    public val strategy: OutboxPollingStrategyType = OutboxPollingStrategyType.ACTIVE_IDLE,
    public val fixed: FixedPollingProperties = FixedPollingProperties(),
    public val activeIdle: ActiveIdlePollingProperties = ActiveIdlePollingProperties(),
    public val adaptive: AdaptivePollingProperties = AdaptivePollingProperties(),
) {

    public fun buildStrategy(): OutboxPollingStrategy = when (strategy) {
        OutboxPollingStrategyType.FIXED -> FixedPollingStrategy.of(fixed.interval)

        OutboxPollingStrategyType.ACTIVE_IDLE -> ActiveIdlePollingStrategy.of(
            activeIdle.activeInterval,
            activeIdle.idleInterval,
        )

        OutboxPollingStrategyType.ADAPTIVE -> AdaptivePollingStrategy.of(adaptive.minInterval, adaptive.maxInterval)
    }
}
