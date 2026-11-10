package pro.vlprojects.semantic.delivery.outbox.polling

public enum class OutboxPollingStrategyType {
    FIXED,
    ACTIVE_IDLE,
    ADAPTIVE,
}
