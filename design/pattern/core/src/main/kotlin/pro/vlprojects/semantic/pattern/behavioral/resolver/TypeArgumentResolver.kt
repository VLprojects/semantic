package pro.vlprojects.semantic.pattern.behavioral.resolver

import java.lang.reflect.TypeVariable
import kotlin.reflect.KClass

public class TypeArgumentResolver<G : Any> private constructor(
    private val typeParameter: TypeVariable<Class<G>>,
) : Resolver<KClass<out G>, KClass<*>> {

    override fun resolveAll(context: KClass<out G>): Collection<KClass<*>> = context.javaObjectType
        .collectTypeVariableMap()
        .resolveRawClassOrNull(typeParameter)
        ?.kotlin
        .let(::listOfNotNull)

    public companion object Factory {

        public fun <G : Any> of(genericType: KClass<G>, index: Int = 0): TypeArgumentResolver<G> = genericType
            .also { type -> checkHasTypeParameter(type, index) }
            .java
            .typeParameters[index]
            .let(::TypeArgumentResolver)
    }
}
