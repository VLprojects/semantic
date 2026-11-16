package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.springframework.core.annotation.Order
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory

@Order(0)
class PipeBorderDecoratorFactory : DecoratorFactory<VisualComponent> {

    override fun decorate(target: VisualComponent) = BorderDecorator(target, "|")
}
