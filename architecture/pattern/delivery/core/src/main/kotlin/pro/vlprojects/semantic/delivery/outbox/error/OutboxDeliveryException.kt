package pro.vlprojects.semantic.delivery.outbox.error

public open class OutboxDeliveryException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
