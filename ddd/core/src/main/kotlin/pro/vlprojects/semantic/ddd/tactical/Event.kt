package pro.vlprojects.semantic.ddd.tactical

public interface Event<A : AggregateRoot<A, I>, out I : Identifier> : DomainObject {

    public val header: EventHeader

    public val aggregateRevision: AggregateRevision<I>
}
