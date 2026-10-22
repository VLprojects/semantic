package pro.vlprojects.semantic.ddd.tactical.id

/**
 * Anything with an identifier: an entity, and also what is not one, such as a read model or a projection.
 *
 * The property is not called `id`. A JPA entity also implements `Persistable` of Spring Data, a Java interface, and its
 * `getId()` would clash with the getter of a Kotlin property named `id` (KT-6653).
 */
public interface Identifiable<out I : Identifier> {
    public val identifier: I
}
