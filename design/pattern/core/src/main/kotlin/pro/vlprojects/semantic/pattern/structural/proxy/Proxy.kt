package pro.vlprojects.semantic.pattern.structural.proxy

import kotlin.reflect.KClass

/**
 * A surrogate that implements the interface of its delegate itself, keeps the delegate private
 * and controls the access to it: later or by rights:
 *
 * ```kotlin
 * class ImageProxy(initialize: () -> Graphic, override val extent: Int) :
 *     Graphic,
 *     Proxy<Graphic> {
 *
 *     private val image = lazy(initialize)
 *
 *     override val delegateClass = Image::class
 *
 *     override fun isDelegateLoaded() = this.image.isInitialized()
 *
 *     override fun draw() = this.image.value.draw()
 * }
 * ```
 */
public interface Proxy<out T : Any> {

    public val delegateClass: KClass<out T>

    public fun isDelegateLoaded(): Boolean
}
