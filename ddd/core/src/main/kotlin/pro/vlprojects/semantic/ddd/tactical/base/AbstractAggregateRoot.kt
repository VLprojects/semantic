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
        get() = NAMES.get(javaClass)

    public val revision: AggregateRevision<I>
        get() = SimpleAggregateRevision(aggregateName, identifier, version)

    protected fun getEvents(): List<Event<A, I>> = events.toList()

    protected fun A.register(event: Event<A, I>): A = apply { events.add(event) }

    protected fun A.clearEvents(): A = apply { events.clear() }

    private companion object {
        val NAMES = object : ClassValue<String>() {
            override fun computeValue(type: Class<*>) = type.simpleName
                .ifEmpty { error("An aggregate without a class name must name itself") }
                .toKebabCase()
        }
    }
}
