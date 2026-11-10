package pro.vlprojects.semantic.delivery.spring.boot.unit.polling

import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingContext
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

internal fun OutboxPollingStrategy.calculateDelayAfter(
    dispatched: Int,
    previousDelay: Duration = Duration.ZERO,
    batchSize: Int = 100,
) = OutboxPollingContext
    .of(dispatched, batchSize, previousDelay)
    .let { context -> this.calculateDelay(context) }
