package pro.vlprojects.semantic.ddd.tactical

public interface AggregateRoot<A : AggregateRoot<A, I>, out I : Identifier> :
    Entity<A, I>,
    Versioned {

    public val revision: Revision<I>
}
