package pro.vlprojects.semantic.pattern.structural.decorator

/**
 * Wraps a ready [target] in a decorator of its type.
 */
public fun interface DecoratorFactory<T : Any> {

    public fun decorate(target: T): T
}
