package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.Message

public fun interface Outbox {

    public fun put(destination: String, message: Message)
}
