package pro.vlprojects.semantic.delivery.outbox

public fun interface EventPublisher<in E : Any> {

    public fun publish(event: E)

    public fun publish(events: Iterable<E>): Unit = events.forEach(::publish)
}
