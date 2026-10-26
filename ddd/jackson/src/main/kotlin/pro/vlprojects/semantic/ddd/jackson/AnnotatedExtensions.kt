package pro.vlprojects.semantic.ddd.jackson

import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotatedConstructor
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedWithParams

private const val VALUE_GETTER_NAME = "getValue"

internal fun Annotated.isValueConstructor(config: MapperConfig<*>) = when (this) {
    is AnnotatedConstructor -> this.isPublic && this.isMemberOfBoxedIdentifier() && this.hasSingleValueParameter(config)
    else -> false
}

internal fun Annotated.isConstructorOfBoxedIdentifier() = when (this) {
    is AnnotatedConstructor -> this.isMemberOfBoxedIdentifier() && this.hasParameters()
    else -> false
}

internal fun Annotated.isValueGetter() = when (this) {
    is AnnotatedMethod -> this.isMemberOfBoxedIdentifier() && this.hasValueGetterName()
    else -> false
}

private fun AnnotatedMember.isMemberOfBoxedIdentifier() = this
    .declaringClass
    .let(BoxedIdentifier::class.java::isAssignableFrom)

private fun AnnotatedConstructor.hasSingleValueParameter(config: MapperConfig<*>) = this.hasSingleParameter() &&
    this.readSingleParameterClass() == this.readBoxedValueClass(config)

private fun AnnotatedMethod.hasValueGetterName() = this.name == VALUE_GETTER_NAME

private fun AnnotatedWithParams.hasParameters() = this.parameterCount > 0

private fun AnnotatedWithParams.hasSingleParameter() = this.parameterCount == 1

// A KClass equates a primitive with its wrapper.
private fun AnnotatedWithParams.readSingleParameterClass() = this.getRawParameterType(0).kotlin

private fun AnnotatedMember.readBoxedValueClass(config: MapperConfig<*>) = this
    .declaringClass
    .let(config::constructType)
    .let { type -> config.typeFactory.findFirstTypeParameter(type, BoxedIdentifier::class.java) }
    .rawClass
    .kotlin
