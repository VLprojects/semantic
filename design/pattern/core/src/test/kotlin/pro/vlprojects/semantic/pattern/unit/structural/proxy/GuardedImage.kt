package pro.vlprojects.semantic.pattern.unit.structural.proxy

import pro.vlprojects.semantic.pattern.structural.proxy.Proxy

class GuardedImage(private val image: Graphic, private val isAllowed: () -> Boolean) :
    Graphic by image,
    Proxy<Graphic> {

    override val delegateClass = this.image::class

    override fun isDelegateLoaded() = true

    override fun draw() = when {
        this.isAllowed() -> this.image.draw()
        else -> throw SecurityException("No rights to draw")
    }
}
