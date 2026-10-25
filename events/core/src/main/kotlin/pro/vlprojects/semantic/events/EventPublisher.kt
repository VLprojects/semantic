package pro.vlprojects.semantic.events

public fun interface EventPublisher<in E : Any> {

    public fun publish(event: E)

    public fun publish(events: Iterable<E>): Unit = events.forEach(::publish)
}
