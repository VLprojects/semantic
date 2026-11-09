package pro.vlprojects.semantic.delivery.outbox.context

public class ContextCollector(private val providers: List<ContextProvider>) {

    public fun collect(payload: Any, builderAction: Context.Builder.() -> Unit = {}): Context = Context
        .builder()
        .also { builder -> providers.forEach { it.provide(payload, builder) } }
        .apply(builderAction)
        .build()
}
