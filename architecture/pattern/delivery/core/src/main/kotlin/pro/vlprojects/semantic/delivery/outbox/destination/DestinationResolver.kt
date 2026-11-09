package pro.vlprojects.semantic.delivery.outbox.destination

import pro.vlprojects.semantic.delivery.outbox.context.Context

public fun interface DestinationResolver {

    public fun resolve(context: Context): Destination
}
