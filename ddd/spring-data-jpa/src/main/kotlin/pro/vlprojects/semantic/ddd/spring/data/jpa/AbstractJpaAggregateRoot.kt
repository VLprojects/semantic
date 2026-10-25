package pro.vlprojects.semantic.ddd.spring.data.jpa

import org.hibernate.Hibernate
import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist

@MappedSuperclass
public abstract class AbstractJpaAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractSpringDataAggregateRoot<A, I>() {

    @PostLoad
    @PostPersist
    private fun markStored() = markPersisted()

    // The final getters of the base read the empty fields of a proxy, so the proxy is compared by its target.
    override fun equals(other: Any?): Boolean = other
        .let(Hibernate::unproxy)
        .let { super.equals(it) }
}
