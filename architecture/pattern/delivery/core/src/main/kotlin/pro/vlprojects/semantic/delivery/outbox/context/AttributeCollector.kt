package pro.vlprojects.semantic.delivery.outbox.context

public class AttributeCollector(private val providers: List<AttributeProvider>) {

    public fun collect(): Map<String, String> = providers
        .flatMap(::readEntries)
        .associate(Map.Entry<String, String>::toPair)

    // An attribute provider must not roll back the business transaction.
    private fun readEntries(provider: AttributeProvider) = try {
        provider.provide().entries
    } catch (_: Exception) {
        emptySet()
    }
}
