package pro.vlprojects.semantic.pattern.integration.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory

class RunnableDecoratorFactory : DecoratorFactory<Runnable> {

    init {
        error("A factory of another type is made")
    }

    override fun decorate(target: Runnable) = target
}
