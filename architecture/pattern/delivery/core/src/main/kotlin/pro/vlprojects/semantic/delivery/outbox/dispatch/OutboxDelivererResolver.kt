package pro.vlprojects.semantic.delivery.outbox.dispatch

import pro.vlprojects.semantic.delivery.outbox.context.Context

public fun interface OutboxDelivererResolver {

    public fun resolve(context: Context): OutboxDeliverer
}
