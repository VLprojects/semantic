package pro.vlprojects.semantic.delivery.outbox.delivery

public enum class DeliveryStatus(public val isTerminal: Boolean) {
    PENDING(isTerminal = false),
    STARTED(isTerminal = false),
    FAILED(isTerminal = false),
    COMPLETED(isTerminal = true),
    CANCELED(isTerminal = true),
}
