package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxRecordMetadata

public fun interface OutboxPayloadSelector {

    public fun matches(payload: Any, metadata: OutboxRecordMetadata): Boolean
}
