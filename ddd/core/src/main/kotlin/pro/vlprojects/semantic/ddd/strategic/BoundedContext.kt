package pro.vlprojects.semantic.ddd.strategic

import pro.vlprojects.semantic.ddd.support.toKebabCase
import kotlin.reflect.KClass

public interface BoundedContext {

    public val name: String
        get() = this::class.simpleName
            ?.toKebabCase()
            ?: error("A bounded context without a class name has no name")
}

public val BoundedContext.packageName: String
    get() = this::class.java.packageName

public operator fun BoundedContext.contains(type: KClass<*>): Boolean = when (val typePackage = type.java.packageName) {
    packageName -> true
    else -> typePackage.startsWith("$packageName.")
}
