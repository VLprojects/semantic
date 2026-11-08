package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxMetadata

public class OutboxRoute(public val target: String, private val selector: OutboxPayloadSelector) {

    public fun matches(payload: Any, metadata: OutboxMetadata): Boolean = selector.matches(payload, metadata)
}
