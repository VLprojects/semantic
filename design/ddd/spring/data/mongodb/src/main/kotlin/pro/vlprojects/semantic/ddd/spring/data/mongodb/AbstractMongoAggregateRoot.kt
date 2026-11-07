package pro.vlprojects.semantic.ddd.spring.data.mongodb

import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Event
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.format.abbreviation.abbreviated
import org.springframework.data.annotation.Transient as StoreTransient

public abstract class AbstractMongoAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractMongoEntity<A, I>(),
    AggregateRoot<A, I> {

    @field:StoreTransient
    @get:DomainEvents
    protected final val events: List<Event<A, I>>
        field = mutableListOf()

    protected final fun A.register(event: Event<A, I>): A = this.apply { events.add(event) }

    @AfterDomainEventPublication
    protected final fun clearEvents() {
        events.clear()
    }

    override fun toString(): String =
        "${this::class.simpleName} [aggregate-root][id: ${identifier.toString().abbreviated()}]"
}
