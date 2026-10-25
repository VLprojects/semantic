package pro.vlprojects.semantic.ddd.strategic

import pro.vlprojects.semantic.format.kebabClassName
import kotlin.reflect.KClass

public interface BoundedContext {

    public val name: String
        get() = kebabClassName

    public val subdomains: Set<Subdomain>
        get() = emptySet()
}

public val BoundedContext.packageName: String
    get() = this::class.java.packageName

public infix fun KClass<*>.hasBoundedContext(context: BoundedContext): Boolean =
    "${this.java.packageName}.".startsWith("${context.packageName}.")
