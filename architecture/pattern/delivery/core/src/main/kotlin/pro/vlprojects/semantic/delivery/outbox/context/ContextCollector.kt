package pro.vlprojects.semantic.delivery.outbox.context

public class ContextCollector(private val providers: List<ContextProvider>) {

    public fun collect(payload: Any, context: Context = Context.of()): Context = providers
        .fold(context) { collected, provider -> provider.provide(payload, collected) }
        .also { check(it.hasTarget()) { "No target was determined for the outbox" } }
}
