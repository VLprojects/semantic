package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class BorderDecorator(
    override val delegate: VisualComponent,
    private val borderWidth: Int,
) : VisualComponent by delegate,
    Decorator<VisualComponent> {

    override fun draw() = "|"
        .repeat(this.borderWidth)
        .let { border -> border + this.delegate.draw() + border }
}
