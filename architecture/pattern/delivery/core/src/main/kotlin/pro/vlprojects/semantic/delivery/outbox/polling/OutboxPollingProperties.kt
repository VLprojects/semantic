package pro.vlprojects.semantic.delivery.outbox.polling

public data class OutboxPollingProperties(
    public val strategy: OutboxPollingStrategy = ActiveIdlePollingStrategy.of(),
)
