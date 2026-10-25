package pro.vlprojects.semantic.ddd.spring.data

import org.springframework.data.domain.Persistable
import org.springframework.data.util.ProxyUtils
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.ddd.tactical.base.AbstractEntity
import org.springframework.data.annotation.Transient as StoreTransient

public abstract class AbstractSpringDataEntity<E : Entity<E, I>, I : Identifier> :
    AbstractEntity<E, I>(),
    Persistable<I> {

    // The modifier keeps JPA from storing the flag, the annotation the other stores.
    @Transient
    @field:StoreTransient
    private var persisted = false

    final override fun getId(): I = identifier

    final override fun isNew(): Boolean = !persisted

    protected open fun markPersisted() {
        persisted = true
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null -> false
        ProxyUtils.getUserClass(this) != ProxyUtils.getUserClass(other) -> false
        else -> identifier == (other as Entity<*, *>).identifier
    }
}
