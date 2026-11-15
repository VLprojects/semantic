package pro.vlprojects.semantic.pattern.structural.decorator

import kotlin.reflect.KClass

/**
 * Marks an annotation that decorates the component it is put on by the factory [value];
 * several decorators take several annotations.
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class Decorated(val value: KClass<out AnnotationDecoratorFactory<*, *>>)
