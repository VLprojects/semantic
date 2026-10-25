package pro.vlprojects.semantic.ddd.spring.data

import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.Revision
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision
import pro.vlprojects.semantic.format.abbreviated

public abstract class AbstractSpringDataAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractSpringDataEntity<A, I>(),
    AggregateRoot<A, I> {

    final override var version: Long = 0
        protected set

    final override val revision: Revision<I>
        get() = BaseRevision.of(this)

    protected val events: List<Event<A, I>>
        field = mutableListOf()

    @get:DomainEvents
    protected val domainEvents: Collection<Event<A, I>>
        get() = events.toList()

    protected fun A.register(event: Event<A, I>): A = apply { events.add(event) }

    @AfterDomainEventPublication
    protected fun clearEvents() {
        events.clear()
    }

    override fun toString(): String =
        "${this::class.simpleName} [aggregate-root][id: ${identifier.toString().abbreviated()}]"
}
