package pro.vlprojects.semantic.delivery.outbox.context

public class ContextCollector(private val providers: List<ContextProvider>) {

    public fun collect(payload: Any): Context = Context
        .builder()
        .also { builder -> providers.forEach { provider -> provider.enrich(payload, builder) } }
        .build()
}
