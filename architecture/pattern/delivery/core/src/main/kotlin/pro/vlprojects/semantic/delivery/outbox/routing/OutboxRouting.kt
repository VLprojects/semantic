package pro.vlprojects.semantic.delivery.outbox.routing

import pro.vlprojects.semantic.delivery.outbox.OutboxMetadata

public class OutboxRouting(private val routes: List<OutboxRoute>) {

    public fun findRoute(payload: Any, metadata: OutboxMetadata): OutboxRoute? = routes
        .firstOrNull { route -> route.matches(payload, metadata) }
}
