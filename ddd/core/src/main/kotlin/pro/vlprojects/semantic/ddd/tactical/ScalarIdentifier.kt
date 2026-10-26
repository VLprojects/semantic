package pro.vlprojects.semantic.ddd.tactical

public interface ScalarIdentifier<out T : Any> : Identifier {

    public val value: T
}
