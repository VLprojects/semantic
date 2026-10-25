package pro.vlprojects.semantic.ddd.spring.data

import org.springframework.data.domain.Persistable
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEntity

public abstract class AbstractSpringDataEntity<E : Entity<E, I>, I : Identifier> :
    AbstractEntity<E, I>(),
    Persistable<I> {

    final override fun getId(): I = identifier
}
