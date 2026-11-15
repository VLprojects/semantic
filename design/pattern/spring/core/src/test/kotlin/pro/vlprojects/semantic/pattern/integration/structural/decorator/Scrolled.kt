package pro.vlprojects.semantic.pattern.integration.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorated

@Target(AnnotationTarget.CLASS)
@Decorated(ScrollDecoratorFactory::class)
annotation class Scrolled
