package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier

public abstract class AbstractAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractEntity<A, I>(),
    AggregateRoot<A, I> {

    abstract override val version: Long

    protected val events: List<Event<A, I>>
        field = mutableListOf()

    protected fun A.register(event: Event<A, I>): A = apply { events.add(event) }

    protected fun clearEvents() {
        events.clear()
    }
}
