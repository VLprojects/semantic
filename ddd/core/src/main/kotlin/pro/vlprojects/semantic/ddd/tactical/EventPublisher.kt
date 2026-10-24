package pro.vlprojects.semantic.ddd.tactical

public fun interface EventPublisher {

    public fun publish(event: Event<*, *>)
}
