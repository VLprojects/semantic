package pro.vlprojects.semantic.ddd.tactical

/**
 * The only way into an aggregate and the boundary of its transaction. Changes leave the aggregate as events, recorded
 * while it changes and released once it is saved.
 */
public interface AggregateRoot<out I : Identifier> : Entity<I>, EventRecordable, Versioned
