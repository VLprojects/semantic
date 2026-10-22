package pro.vlprojects.semantic.ddd.model

import pro.vlprojects.semantic.ddd.id.Identifiable
import pro.vlprojects.semantic.ddd.id.Identifier

/**
 * An object with an identity that outlives the changes of its state.
 *
 * Equality is left to the implementation: the JPA module compares by identifier and sees through Hibernate proxies.
 */
public interface Entity<out I : Identifier> : Identifiable<I>
