package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.AnnotationDecoratorFactory

class BorderDecoratorFactory : AnnotationDecoratorFactory<Bordered, VisualComponent> {

    override fun decorate(delegate: VisualComponent, annotation: Bordered) =
        BorderDecorator(delegate, annotation.width)
}
