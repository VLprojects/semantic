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

    private val registered = mutableListOf<Event<A, I>>()

    protected open val events: List<Event<A, I>>
        get() = registered

    protected fun A.register(event: Event<A, I>): A = apply { registered.add(event) }

    protected open fun clearEvents() {
        registered.clear()
    }

    override fun toString(): String =
        "${this::class.simpleName} [aggregate-root][id: ${identifier.toString().abbreviated()}]"
}
