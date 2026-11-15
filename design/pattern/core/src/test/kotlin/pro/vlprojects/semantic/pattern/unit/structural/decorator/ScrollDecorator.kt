package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class ScrollDecorator(override val delegate: VisualComponent) :
    VisualComponent by delegate,
    Decorator<VisualComponent> {

    override fun draw() = this.delegate.draw() + "↕"
}
