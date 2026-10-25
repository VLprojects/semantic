package pro.vlprojects.semantic.ddd.spring.data.jpa

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
public abstract class AbstractJpaEntity<E : Entity<E, I>, I : Identifier> : AbstractSpringDataEntity<E, I>()
