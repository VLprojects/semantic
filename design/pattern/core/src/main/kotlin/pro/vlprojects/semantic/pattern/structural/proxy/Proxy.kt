package pro.vlprojects.semantic.pattern.structural.proxy

import kotlin.reflect.KClass

/**
 * A surrogate that implements the interface of its target itself, keeps the target private
 * and controls the access to it: later or by rights.
 */
public interface Proxy<out T : Any> {

    public val targetClass: KClass<out T>

    public fun isTargetInitialized(): Boolean
}
