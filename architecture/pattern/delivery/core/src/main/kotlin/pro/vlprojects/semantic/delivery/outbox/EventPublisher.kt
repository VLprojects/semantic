package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.ddd.tactical.Event

public fun interface EventPublisher<in E : Event<*, *>> {

    public fun publish(event: E)

    public fun publish(events: Iterable<E>): Unit = events.forEach(::publish)
}
