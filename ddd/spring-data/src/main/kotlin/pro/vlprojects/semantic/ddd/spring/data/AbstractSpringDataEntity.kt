package pro.vlprojects.semantic.ddd.spring.data

import org.springframework.data.domain.Persistable
import org.springframework.data.util.ProxyUtils
import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import pro.vlprojects.semantic.format.abbreviation.abbreviated
import org.springframework.data.annotation.Transient as StoreTransient

public abstract class AbstractSpringDataEntity<E : Entity<E, I>, I : Identifier> :
    Entity<E, I>,
    Persistable<I> {

    // Open, so that a lazy proxy of a store answers with its own key and not with its empty field.
    override lateinit var identifier: I
        protected set

    override var version: Long = 0
        protected set

    // The modifier keeps JPA from storing the flag, the annotation the other stores.
    @Transient
    @field:StoreTransient
    private var persisted = false

    protected open val invariants: List<BusinessRule<E>>
        get() = emptyList()

    final override fun getId(): I = identifier

    final override fun isNew(): Boolean = !persisted

    protected fun markPersisted() {
        persisted = true
    }

    protected fun E.change(mutation: E.() -> Unit): E = apply {
        mutation()
        invariants.check(this)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null -> false
        ProxyUtils.getUserClass(this) != ProxyUtils.getUserClass(other) -> false
        else -> identifier == (other as Entity<*, *>).identifier
    }

    override fun hashCode(): Int = identifier.hashCode()

    override fun toString(): String = "${this::class.simpleName} [entity][id: ${identifier.toString().abbreviated()}]"
}
