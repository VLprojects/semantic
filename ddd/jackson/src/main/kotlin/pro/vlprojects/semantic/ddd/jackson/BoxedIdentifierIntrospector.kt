package pro.vlprojects.semantic.ddd.jackson

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.NopAnnotationIntrospector

internal object BoxedIdentifierIntrospector : NopAnnotationIntrospector() {

    override fun findCreatorAnnotation(config: MapperConfig<*>, annotated: Annotated) = when {
        annotated.isValueConstructor(config) -> JsonCreator.Mode.DELEGATING
        annotated.isConstructorOfBoxedIdentifier() -> JsonCreator.Mode.DISABLED
        else -> null
    }

    override fun hasAsValue(config: MapperConfig<*>, annotated: Annotated) = when {
        annotated.isValueGetter() -> true
        else -> null
    }
}
