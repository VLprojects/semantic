package pro.vlprojects.semantic.ddd.spring.data.jpa

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import pro.vlprojects.semantic.ddd.tactical.Entity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Version

@MappedSuperclass
public abstract class AbstractJpaEntity<E : Entity<E, I>, I : BoxedIdentifier<*>> : AbstractSpringDataEntity<E, I>() {

    @EmbeddedId
    @AttributeOverride(name = "value", column = Column(name = "id", updatable = false, nullable = false))
    override lateinit var identifier: I
        protected set

    @Version
    override var version: Long = 0
        protected set

    @PostLoad
    @PostPersist
    override fun markPersisted(): Unit = super.markPersisted()
}
