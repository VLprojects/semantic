package pro.vlprojects.semantic.pattern.structural.decorator.spring

import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * Marks an annotation that decorates the bean it is put on by the factory [value];
 * a class with such an annotation is a component of the scan, several decorators take several annotations.
 */
@Component
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class Decorated(val value: KClass<out AnnotationDecoratorFactory<*, *>>)
