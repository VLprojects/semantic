package pro.vlprojects.semantic.pattern.structural.decorator

/**
 * Wraps a ready [delegate] in a decorator of its type.
 */
public fun interface DecoratorFactory<T : Any> {

    public fun decorate(delegate: T): T
}
