package pro.vlprojects.semantic.pattern.structural.decorator

import kotlin.reflect.KClass

/**
 * A wrapper that implements the interface of its component itself, keeps the delegate it wraps private,
 * forwards each request to it and adds a responsibility before or after:
 *
 * ```kotlin
 * class ScrollDecorator(private val delegate: VisualComponent) :
 *     VisualComponent by delegate,
 *     Decorator<VisualComponent> {
 *
 *     override val delegateClass = this.delegate::class
 *
 *     override fun draw() = this.delegate.draw() + "↕"
 * }
 * ```
 */
public interface Decorator<out T : Any> {

    public val targetClass: KClass<out T>
}
