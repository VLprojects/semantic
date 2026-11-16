package pro.vlprojects.semantic.pattern.behavioral.resolver

import java.lang.reflect.TypeVariable

internal fun checkHasTypeParameter(genericType: Class<*>, index: Int) {
    val typeParameterNames = genericType.typeParameters.map(TypeVariable<*>::getName)

    require(index in typeParameterNames.indices) {
        "${genericType.name} has no type parameter at index $index, its type parameters: $typeParameterNames"
    }
}
