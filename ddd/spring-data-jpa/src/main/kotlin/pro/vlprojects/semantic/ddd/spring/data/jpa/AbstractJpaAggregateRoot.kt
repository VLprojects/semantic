package pro.vlprojects.semantic.ddd.spring.data.jpa

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Version

// Repeats AbstractJpaEntity: the root extends the root of Spring Data, and a class has one superclass.
@MappedSuperclass
public abstract class AbstractJpaAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractSpringDataAggregateRoot<A, I>() {

    @EmbeddedId
    @AttributeOverride(name = "value", column = Column(name = "id", updatable = false, nullable = false))
    override lateinit var identifier: I
        protected set

    @Version
    override var version: Long = 0
        protected set

    @PostLoad
    @PostPersist
    override fun markPersisted() {
        super.markPersisted()
    }
}
