package pro.vlprojects.semantic.delivery.outbox.error

public open class OutboxRoutingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
