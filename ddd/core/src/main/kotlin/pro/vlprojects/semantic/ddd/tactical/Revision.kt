package pro.vlprojects.semantic.ddd.tactical

public interface Revision<out I : Identifier> :
    Identifiable<I>,
    Versioned {

    public val name: String
}
