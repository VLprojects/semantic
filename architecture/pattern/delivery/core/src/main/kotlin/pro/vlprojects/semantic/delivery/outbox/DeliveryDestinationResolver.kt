package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.context.Context

public fun interface DeliveryDestinationResolver {

    public fun resolve(context: Context): DeliveryDestination
}
