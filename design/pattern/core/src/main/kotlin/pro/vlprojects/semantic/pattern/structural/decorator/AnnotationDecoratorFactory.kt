package pro.vlprojects.semantic.pattern.structural.decorator

/**
 * Wraps a [target] that carries an [annotation] marked with [Decorated]
 * in a decorator configured by the attributes of the annotation.
 */
public interface AnnotationDecoratorFactory<A : Annotation, T : Any> {

    public fun decorate(target: T, annotation: A): T
}
