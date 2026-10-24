package pro.vlprojects.semantic.ddd.architecture.event

import pro.vlprojects.semantic.ddd.tactical.Event

public fun interface EventPublisher {

    public fun publish(event: Event<*, *>)
}
