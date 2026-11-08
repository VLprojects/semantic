package pro.vlprojects.semantic.delivery.outbox

public enum class DeliveryStatus {
    AWAITING,
    IN_PROGRESS,
    FAILED,
    COMPLETED,
    ABANDONED,
    ;

    public val isTerminal: Boolean
        get() = when (this) {
            COMPLETED -> true
            ABANDONED -> true
            else -> false
        }
}
