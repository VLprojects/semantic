package pro.vlprojects.semantic.ddd.tactical

public interface Entity<E : Entity<E, I>, out I : Identifier> :
    DomainObject,
    Identifiable<I>,
    Versioned
