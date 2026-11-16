package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.springframework.context.annotation.Lazy
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory

@Lazy
class RunnableDecoratorFactory : DecoratorFactory<Runnable> {

    init {
        error("A factory of another type is made")
    }

    override fun decorate(target: Runnable) = target
}
