package pro.vlprojects.semantic.ddd.spring.data.mongodb

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

public abstract class AbstractMongoEntity<E : Entity<E, I>, I : Identifier> : AbstractSpringDataEntity<E, I>() {

    // No field of its own: Spring Data maps the field of the core, and a second one would clash with it.
    @get:Id
    override var identifier: I
        get() = super.identifier
        protected set(value) {
            super.identifier = value
        }

    @get:Version
    override var version: Long
        get() = super.version
        protected set(value) {
            super.version = value
        }

    internal fun markPersistedByStore() {
        markPersisted()
    }
}
