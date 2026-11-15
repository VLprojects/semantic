package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class BorderDecorator(
    private val delegate: VisualComponent,
    private val borderWidth: Int,
) : VisualComponent by delegate,
    Decorator<VisualComponent> {

    override val delegateClass = this.delegate::class

    override fun draw() = "|"
        .repeat(this.borderWidth)
        .let { border -> border + this.delegate.draw() + border }
}
