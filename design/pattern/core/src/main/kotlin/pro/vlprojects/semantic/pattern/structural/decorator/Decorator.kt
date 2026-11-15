package pro.vlprojects.semantic.pattern.structural.decorator

import kotlin.reflect.KClass

/**
 * A wrapper that implements the interface of its component itself, keeps the delegate it wraps private,
 * forwards each request to it and adds a responsibility before or after.
 */
public interface Decorator<out T : Any> {

    public val delegateClass: KClass<out T>
}
