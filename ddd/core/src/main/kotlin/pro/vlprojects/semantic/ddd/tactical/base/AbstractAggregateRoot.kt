package pro.vlprojects.semantic.ddd.tactical.base

import pro.vlprojects.semantic.ddd.support.abbreviated
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.Revision

public abstract class AbstractAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractEntity<A, I>(),
    AggregateRoot<A, I> {

    final override var version: Long = 0
        protected set

    final override val revision: Revision<I>
        get() = BaseRevision.of(this)

    protected val events: List<Event<A, I>>
        field = mutableListOf()

    protected fun A.register(event: Event<A, I>): A = apply { events.add(event) }

    protected open fun clearEvents() {
        events.clear()
    }

    override fun toString(): String =
        "${this::class.simpleName} [aggregate-root][id: ${identifier.toString().abbreviated()}]"
}
