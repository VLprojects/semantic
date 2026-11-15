package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory

@Configuration(proxyBeanMethods = false)
class DecorationConfiguration {

    @Bean
    @Order(1)
    fun borderDecoratorFactory() = DecoratorFactory<VisualComponent> { target -> BorderDecorator(target, "#") }

    @Bean
    @Order(2)
    fun scrollDecoratorFactory() = DecoratorFactory<VisualComponent>(::ScrollDecorator)
}
