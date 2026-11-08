package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxRecordMetadata

public class OutboxRoute(public val target: String, private val selector: OutboxPayloadSelector) {

    public fun matches(payload: Any, metadata: OutboxRecordMetadata): Boolean = selector.matches(payload, metadata)
}
