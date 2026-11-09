package pro.vlprojects.semantic.delivery.outbox.error

public class OutboxDestinationUnavailableException(
    message: String,
    cause: Throwable? = null,
) : OutboxDeliveryException(message, cause)
