package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.toKebabCase
import pro.vlprojects.semantic.ddd.tactical.AggregateRevision
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier

public abstract class AbstractAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractEntity<A, I>(),
    AggregateRoot<A, I> {

    private val events = mutableListOf<Event<A, I>>()

    // Receivers route by it: override with the old name when the class is renamed.
    protected open val aggregateName: String
        get() {
            val name = checkNotNull(classOf(this).simpleName) { "An aggregate without a class name must name itself" }
            return name.toKebabCase()
        }

    public val revision: AggregateRevision<I>
        get() = SimpleAggregateRevision(aggregateName, identifier, version)

    protected fun getEvents(): List<Event<A, I>> = events.toList()

    protected fun register(event: Event<A, I>): AbstractAggregateRoot<A, I> = apply { events.add(event) }

    protected fun clearEvents(): AbstractAggregateRoot<A, I> = apply { events.clear() }
}
