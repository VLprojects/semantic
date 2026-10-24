package pro.vlprojects.semantic.ddd.spring.data

import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import org.springframework.data.domain.Persistable
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractAggregateRoot

public abstract class SpringDataAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractAggregateRoot<A, I>(),
    Persistable<I> {

    final override fun getId(): I = identifier

    @get:DomainEvents
    protected val domainEvents: Collection<Event<A, I>>
        get() = events.toList()

    @AfterDomainEventPublication
    final override fun clearEvents() {
        super.clearEvents()
    }
}
