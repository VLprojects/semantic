package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public fun interface OutboxPollingStrategy {

    public fun calculateDelay(context: OutboxPollingContext): Duration
}
