package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxRecordMetadata

public class OutboxPayloadTypeSelector(private val type: Class<*>) : OutboxPayloadSelector {

    override fun matches(payload: Any, metadata: OutboxRecordMetadata): Boolean = type.isInstance(payload)
}
