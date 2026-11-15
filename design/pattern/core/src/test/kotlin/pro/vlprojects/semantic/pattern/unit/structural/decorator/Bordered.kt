package pro.vlprojects.semantic.pattern.unit.structural.decorator

import pro.vlprojects.semantic.pattern.structural.decorator.Decorate

@Target(AnnotationTarget.CLASS)
@Decorate(BorderDecoratorFactory::class)
annotation class Bordered(val width: Int)
