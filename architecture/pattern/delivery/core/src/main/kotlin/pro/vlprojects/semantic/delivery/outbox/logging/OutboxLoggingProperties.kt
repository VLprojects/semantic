package pro.vlprojects.semantic.delivery.outbox.logging

import org.slf4j.event.Level

public data class OutboxLoggingProperties(
    public val failure: LoggingEventProperties = LoggingEventProperties(Level.WARN),
    public val cancellation: LoggingEventProperties = LoggingEventProperties(Level.ERROR),
)
