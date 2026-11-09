package pro.vlprojects.semantic.delivery.outbox.error

public class OutboxDeliveryRejectedException(
    message: String,
    cause: Throwable? = null,
) : OutboxDeliveryException(message, cause)
