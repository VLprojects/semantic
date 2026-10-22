package pro.vlprojects.semantic.ddd.model

import pro.vlprojects.semantic.ddd.identity.Identifier

/**
 * An object with an identity that outlives the changes of its state.
 *
 * The property is not called `id`. A JPA entity also implements `Persistable` of Spring Data, a Java interface, and its
 * `getId()` would clash with the getter of a Kotlin property named `id` (KT-6653).
 *
 * Equality is left to the implementation: the JPA module compares by identifier and sees through Hibernate proxies.
 */
public interface Entity<out I : Identifier> {
    public val identifier: I
}
