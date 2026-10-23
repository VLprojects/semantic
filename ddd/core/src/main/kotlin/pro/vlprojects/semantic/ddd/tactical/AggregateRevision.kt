package pro.vlprojects.semantic.ddd.tactical

public interface AggregateRevision<out I : Identifier> :
    Identifiable<I>,
    Versioned {

    public val name: String
}
