package pro.vlprojects.semantic.pattern.integration.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.AnnotationDecoratorFactory

class ScrollDecoratorFactory : AnnotationDecoratorFactory<Scrolled, VisualComponent> {

    override fun decorate(target: VisualComponent, annotation: Scrolled) = ScrollDecorator(target)
}
