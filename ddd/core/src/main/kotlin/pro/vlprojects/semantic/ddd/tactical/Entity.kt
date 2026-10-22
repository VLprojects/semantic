package pro.vlprojects.semantic.ddd.tactical

/**
 * An object with an identity that outlives the changes of its state.
 *
 * [E] is the entity itself, so that a contract can speak of it: a method may return the entity, and a type built on
 * it gets the identifier type from it.
 *
 * Equality is left to the implementation: the JPA module compares by identifier and sees through Hibernate proxies.
 */
public interface Entity<E : Entity<E, I>, out I : Identifier> : Identifiable<I>
