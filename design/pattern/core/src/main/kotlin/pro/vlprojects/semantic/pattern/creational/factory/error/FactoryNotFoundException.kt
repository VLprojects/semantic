package pro.vlprojects.semantic.pattern.creational.factory.error

import pro.vlprojects.semantic.pattern.creational.factory.Factory

public class FactoryNotFoundException(
    definition: Factory.Definition,
) : NoSuchElementException("No factory creates an instance of ${definition.javaClass.name}")
