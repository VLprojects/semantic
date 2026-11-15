package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class BorderDecorator(
    override val delegate: VisualComponent,
    private val border: String,
) : VisualComponent by delegate,
    Decorator<VisualComponent> {

    override fun draw() = this.border + this.delegate.draw() + this.border
}
