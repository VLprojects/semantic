package pro.vlprojects.semantic.delivery.unit.outbox.polling

import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingContext
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

internal fun OutboxPollingStrategy.calculateDurationAfterBatch(
    dispatched: Int,
    previousDurationBeforePolling: Duration = Duration.ZERO,
    batchSize: Int = 100,
) = OutboxPollingContext
    .of(dispatched, batchSize, previousDurationBeforePolling)
    .let { context -> this.calculateDurationBeforePolling(context) }
