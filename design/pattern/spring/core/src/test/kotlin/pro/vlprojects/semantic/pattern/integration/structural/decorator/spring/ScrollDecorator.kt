package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class ScrollDecorator(override val delegate: VisualComponent) :
    VisualComponent by delegate,
    Decorator<VisualComponent> {

    override fun draw() = this.delegate.draw() + "↕"
}
