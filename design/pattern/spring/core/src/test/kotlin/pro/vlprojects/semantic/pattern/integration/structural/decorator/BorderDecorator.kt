package pro.vlprojects.semantic.pattern.integration.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorator

class BorderDecorator(
    private val delegate: VisualComponent,
    private val border: String,
) : VisualComponent by delegate,
    Decorator<VisualComponent> {

    override val delegateClass = this.delegate::class

    override fun draw() = this.border + this.delegate.draw() + this.border
}
