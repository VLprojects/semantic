package pro.vlprojects.semantic.delivery.outbox.context

public class OutboxContextCollector(private val providers: List<OutboxContextProvider>) {

    public fun collect(): Map<String, String> = providers
        .flatMap(::readEntries)
        .associate(Map.Entry<String, String>::toPair)

    // A context provider must not roll back the business transaction.
    private fun readEntries(provider: OutboxContextProvider) = try {
        provider.provide().entries
    } catch (_: Exception) {
        emptySet()
    }
}
