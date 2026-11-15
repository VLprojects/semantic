package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorated

@Target(AnnotationTarget.CLASS)
@Decorated(BorderDecoratorFactory::class)
annotation class Bordered(val width: Int)
