package pro.vlprojects.semantic.pattern.integration.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory

class DashBorderDecoratorFactory : DecoratorFactory<VisualComponent> {

    override fun decorate(target: VisualComponent) = BorderDecorator(target, "-")
}
