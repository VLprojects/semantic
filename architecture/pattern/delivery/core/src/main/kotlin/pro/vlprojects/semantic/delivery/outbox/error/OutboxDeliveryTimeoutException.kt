package pro.vlprojects.semantic.delivery.outbox.error

public class OutboxDeliveryTimeoutException(
    message: String,
    cause: Throwable? = null,
) : OutboxDeliveryException(message, cause)
