package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxMetadata

public fun interface OutboxPayloadSelector {

    public fun matches(payload: Any, metadata: OutboxMetadata): Boolean
}
