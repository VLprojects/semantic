package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

import pro.vlprojects.semantic.pattern.structural.decorator.spring.AnnotationDecoratorFactory

class BorderDecoratorFactory(private val style: BorderStyle) : AnnotationDecoratorFactory<Bordered, VisualComponent> {

    init {
        this.style.factories++
    }

    override fun decorate(target: VisualComponent, annotation: Bordered) = this.style.symbol
        .plus(annotation.width)
        .let { border -> BorderDecorator(target, border) }
}
