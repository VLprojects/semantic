package pro.vlprojects.semantic.ddd.strategic

import pro.vlprojects.semantic.format.toKebabCase
import kotlin.reflect.KClass

public interface BoundedContext {

    public val name: String
        get() = this::class.simpleName
            ?.toKebabCase()
            ?: error("A bounded context without a class name has no name")
}

public val BoundedContext.packageName: String
    get() = this::class.java.packageName

public infix fun KClass<*>.hasBoundedContext(context: BoundedContext): Boolean =
    when (val typePackage = this.java.packageName) {
        context.packageName -> true
        else -> typePackage.startsWith("${context.packageName}.")
    }
