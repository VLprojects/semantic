package pro.vlprojects.semantic.delivery.observability

import pro.vlprojects.semantic.delivery.outbox.Outbox
import pro.vlprojects.semantic.delivery.outbox.OutboxRepository

public class InstrumentedOutboxRepository(
    private val delegate: OutboxRepository,
    private val instrumentation: OutboxInstrumentation,
) : OutboxRepository by delegate {

    override fun save(outbox: Outbox) {
        delegate.save(outbox)
        instrumentation.onScheduled(outbox.metadata)
    }
}
