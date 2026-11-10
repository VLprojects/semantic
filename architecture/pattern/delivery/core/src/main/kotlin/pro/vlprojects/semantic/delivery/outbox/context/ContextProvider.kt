package pro.vlprojects.semantic.delivery.outbox.context

public fun interface ContextProvider {

    public fun enrich(payload: Any, builder: Context.Builder)
}
