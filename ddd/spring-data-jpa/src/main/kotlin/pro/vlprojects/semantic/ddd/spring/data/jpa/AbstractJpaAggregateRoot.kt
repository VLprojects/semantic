package pro.vlprojects.semantic.ddd.spring.data.jpa

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataAggregateRoot
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
public abstract class AbstractJpaAggregateRoot<A : AggregateRoot<A, I>, I : Identifier> :
    AbstractSpringDataAggregateRoot<A, I>()
