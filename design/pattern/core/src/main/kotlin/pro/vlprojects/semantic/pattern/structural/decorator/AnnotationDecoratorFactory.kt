package pro.vlprojects.semantic.pattern.structural.decorator

/**
 * Wraps a component that carries an annotation marked with [Decorate]
 * in a decorator configured by the attributes of the annotation.
 */
public interface AnnotationDecoratorFactory<A : Annotation, T : Any> {

    public fun decorate(component: T, annotation: A): T
}
