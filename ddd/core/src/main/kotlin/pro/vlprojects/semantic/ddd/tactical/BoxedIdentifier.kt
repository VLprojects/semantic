package pro.vlprojects.semantic.ddd.tactical

import java.io.Serializable

public interface BoxedIdentifier<out T : Serializable> : Identifier {

    public val value: T
}
