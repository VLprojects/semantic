package pro.vlprojects.semantic.delivery.outbox.dispatch

import pro.vlprojects.semantic.delivery.outbox.context.Context

public fun interface DeliveryDestinationResolver {

    public fun resolve(context: Context): DeliveryDestination
}
