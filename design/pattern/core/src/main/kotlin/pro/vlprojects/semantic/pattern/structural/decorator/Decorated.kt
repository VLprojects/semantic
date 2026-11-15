package pro.vlprojects.semantic.pattern.structural.decorator

import kotlin.reflect.KClass

/**
 * Marks an annotation that decorates the component it is put on
 * by the one of [value] whose component type the component has.
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class Decorated(vararg val value: KClass<out AnnotationDecoratorFactory<*, *>>)
