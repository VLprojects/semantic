package pro.vlprojects.semantic.delivery.outbox

public enum class DeliveryStatus(public val isTerminal: Boolean) {
    PENDING(isTerminal = false),
    PROCESSING(isTerminal = false),
    FAILED(isTerminal = false),
    COMPLETED(isTerminal = true),
    CANCELED(isTerminal = true),
}
