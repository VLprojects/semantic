package pro.vlprojects.semantic.ddd.spring.data.jpa

import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.ScalarIdentifier
import pro.vlprojects.semantic.format.abbreviation.abbreviated
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
public abstract class AbstractJpaAggregateRoot<A : AggregateRoot<A, I>, I : ScalarIdentifier<*>> :
    AbstractJpaEntity<A, I>(),
    AggregateRoot<A, I> {

    @Transient
    @get:DomainEvents
    protected final val events: List<Event<A, I>>
        field = mutableListOf()

    protected final fun A.register(event: Event<A, I>): A = apply { events.add(event) }

    @AfterDomainEventPublication
    protected final fun clearEvents() {
        events.clear()
    }

    override fun toString(): String =
        "${this::class.simpleName} [aggregate-root][id: ${identifier.toString().abbreviated()}]"
}
