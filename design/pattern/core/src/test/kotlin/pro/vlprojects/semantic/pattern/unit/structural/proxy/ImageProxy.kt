package pro.vlprojects.semantic.pattern.unit.structural.proxy

import pro.vlprojects.semantic.pattern.structural.proxy.Proxy
import kotlin.reflect.KClass

class ImageProxy(
    override val delegateClass: KClass<out Graphic>,
    initializeDelegate: () -> Graphic,
    override val extent: Int,
) : Graphic,
    Proxy<Graphic> {

    private val image = lazy(initializeDelegate)

    override fun isDelegateLoaded() = this.image.isInitialized()

    override fun draw() = this.image.value.draw()
}
