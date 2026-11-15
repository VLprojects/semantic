package pro.vlprojects.semantic.pattern.integration.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorated

@Target(AnnotationTarget.CLASS)
@Decorated(BorderDecoratorFactory::class)
annotation class Underlined
