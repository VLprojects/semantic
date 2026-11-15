package pro.vlprojects.semantic.pattern.unit.structural.proxy

import pro.vlprojects.semantic.pattern.structural.proxy.Proxy
import kotlin.reflect.KClass

class ImageProxy(override val targetClass: KClass<out Graphic>, initialize: () -> Graphic, override val extent: Int) :
    Graphic,
    Proxy<Graphic> {

    private val image = lazy(initialize)

    override fun isTargetInitialized() = this.image.isInitialized()

    override fun draw() = this.image.value.draw()
}
