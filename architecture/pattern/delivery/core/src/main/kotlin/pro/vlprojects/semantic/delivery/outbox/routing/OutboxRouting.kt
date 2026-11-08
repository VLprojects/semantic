package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxRecordMetadata

public class OutboxRouting(private val routes: List<OutboxRoute>) {

    public fun findRoute(payload: Any, metadata: OutboxRecordMetadata): OutboxRoute? = routes
        .firstOrNull { route -> route.matches(payload, metadata) }
}
