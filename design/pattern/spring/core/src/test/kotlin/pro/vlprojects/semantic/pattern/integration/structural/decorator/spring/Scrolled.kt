package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

import pro.vlprojects.semantic.pattern.structural.decorator.spring.Decorated

@Target(AnnotationTarget.CLASS)
@Decorated(ScrollDecoratorFactory::class)
annotation class Scrolled
