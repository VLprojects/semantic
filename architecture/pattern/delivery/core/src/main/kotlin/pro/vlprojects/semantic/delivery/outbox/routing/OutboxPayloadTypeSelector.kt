package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxMetadata

public class OutboxPayloadTypeSelector(private val type: Class<*>) : OutboxPayloadSelector {

    override fun matches(payload: Any, metadata: OutboxMetadata): Boolean = type.isInstance(payload)
}
