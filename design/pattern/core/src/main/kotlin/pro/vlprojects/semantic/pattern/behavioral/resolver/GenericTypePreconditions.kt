package pro.vlprojects.semantic.pattern.behavioral.resolver

import java.lang.reflect.TypeVariable
import kotlin.reflect.KClass

internal fun checkHasTypeParameter(genericType: KClass<*>, index: Int) {
    val typeParameterNames = genericType.java.typeParameters.map(TypeVariable<*>::getName)

    require(index in typeParameterNames.indices) {
        "${genericType.java.name} has no type parameter at index $index, its type parameters: $typeParameterNames"
    }
}
