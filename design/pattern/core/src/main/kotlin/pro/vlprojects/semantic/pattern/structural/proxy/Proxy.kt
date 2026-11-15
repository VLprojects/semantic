package pro.vlprojects.semantic.pattern.structural.proxy

import kotlin.reflect.KClass

/**
 * A surrogate that implements the interface of its delegate itself, keeps the delegate private
 * and controls the access to it: later or by rights.
 */
public interface Proxy<out T : Any> {

    public val delegateClass: KClass<out T>

    public fun isDelegateLoaded(): Boolean
}
