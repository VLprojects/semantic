package pro.vlprojects.semantic.ddd.tactical

/**
 * The only way into an aggregate and the boundary of its transaction. Changes leave the aggregate as events, recorded
 * while it changes and released once it is saved.
 *
 * [A] is the aggregate itself, so that a contract can speak of it: a method may return the aggregate, and a type
 * built on it, such as a repository, gets the identifier type from it.
 *
 * ```kotlin
 * class Cart(override val identifier: CartId) : AggregateRoot<Cart, CartId>
 * ```
 */
public interface AggregateRoot<A : AggregateRoot<A, I>, out I : Identifier> :
    Entity<A, I>, EventReleasable<Event<A>>, Versioned
