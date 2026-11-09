package pro.vlprojects.semantic.delivery.outbox.context

public fun interface ContextProvider {

    public fun provide(payload: Any, context: Context): Context
}
