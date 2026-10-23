package pro.vlprojects.semantic.ddd.tactical

public interface Event<A : AggregateRoot<A, I>, out I : Identifier> {
    public val header: EventHeader

    public val aggregate: AggregateRevision<I>
}
