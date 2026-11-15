package pro.vlprojects.semantic.pattern.structural.decorator

/**
 * Wraps a ready component in a decorator of its type.
 */
public fun interface DecoratorFactory<C : Any> {

    public fun decorate(component: C): C
}
