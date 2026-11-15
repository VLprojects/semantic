package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class ScrollDecorator(private val delegate: VisualComponent) :
    VisualComponent by delegate,
    Decorator<VisualComponent> {

    override val targetClass = this.delegate::class

    override fun draw() = this.delegate.draw() + "↕"
}
