package pro.vlprojects.semantic.ddd.tactical

public interface BoxedIdentifier<out T : Any> : Identifier {

    public val value: T
}
