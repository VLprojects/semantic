package pro.vlprojects.semantic.delivery.outbox.delivery

import pro.vlprojects.semantic.delivery.outbox.Outbox

public fun interface OutboxDeliverer {

    public fun deliver(outbox: Outbox)
}
