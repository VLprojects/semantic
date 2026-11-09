package pro.vlprojects.semantic.delivery.outbox.context

public class ContextCollector(private val providers: List<ContextProvider>) {

    public fun collect(payload: Any, explicit: ContextBuilder.() -> Unit = {}): Context = ContextBuilder()
        .also { builder -> providers.forEach { it.provide(payload, builder) } }
        .apply(explicit)
        .build()
}
