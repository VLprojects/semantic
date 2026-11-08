package pro.vlprojects.semantic.delivery.outbox

public enum class DeliveryStatus(public val isTerminal: Boolean) {
    AWAITING(isTerminal = false),
    IN_PROGRESS(isTerminal = false),
    FAILED(isTerminal = false),
    COMPLETED(isTerminal = true),
    ABANDONED(isTerminal = true),
}
