package pro.vlprojects.semantic.delivery.outbox.context

public fun interface OutboxContextProvider {

    public fun provide(): Map<String, String>
}
