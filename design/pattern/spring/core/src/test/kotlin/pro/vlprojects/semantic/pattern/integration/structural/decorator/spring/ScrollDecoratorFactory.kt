package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

import pro.vlprojects.semantic.pattern.structural.decorator.spring.AnnotationDecoratorFactory

class ScrollDecoratorFactory : AnnotationDecoratorFactory<Scrolled, VisualComponent> {

    override fun decorate(target: VisualComponent, annotation: Scrolled) = ScrollDecorator(target)
}
