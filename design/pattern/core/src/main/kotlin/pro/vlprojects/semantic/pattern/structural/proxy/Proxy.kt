package pro.vlprojects.semantic.pattern.structural.proxy

import kotlin.reflect.KClass

/**
 * A surrogate that implements the interface of its delegate itself, keeps the delegate private
 * and controls the access to it: later or by rights.
 *
 * A virtual proxy knows the class of its delegate before it loads it:
 *
 * ```kotlin
 * class ImageProxy(initializeDelegate: () -> Graphic, override val extent: Int) :
 *     Graphic,
 *     Proxy<Graphic> {
 *
 *     private val image = lazy(initializeDelegate)
 *
 *     override val delegateClass = Image::class
 *
 *     override fun isDelegateLoaded() = this.image.isInitialized()
 *
 *     override fun draw() = this.image.value.draw()
 * }
 * ```
 *
 * A protection proxy takes its delegate ready:
 *
 * ```kotlin
 * class GuardedImage(private val image: Graphic, private val isAllowed: () -> Boolean) :
 *     Graphic by image,
 *     Proxy<Graphic> {
 *
 *     override val delegateClass = this.image::class
 *
 *     override fun isDelegateLoaded() = true
 *
 *     override fun draw() = when {
 *         this.isAllowed() -> this.image.draw()
 *         else -> throw SecurityException("No rights to draw")
 *     }
 * }
 * ```
 */
public interface Proxy<out T : Any> {

    public val delegateClass: KClass<out T>

    public fun isDelegateLoaded(): Boolean
}
