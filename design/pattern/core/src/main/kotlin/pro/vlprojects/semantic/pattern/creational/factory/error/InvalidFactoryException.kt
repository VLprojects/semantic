package pro.vlprojects.semantic.pattern.creational.factory.error

import pro.vlprojects.semantic.pattern.creational.factory.Factory

public class InvalidFactoryException(
    factory: Factory<*, *>,
) : IllegalArgumentException(
        "Cannot resolve the definition that ${factory.javaClass.name} takes: declare the factory as a class that parameterizes Factory<D, T>",
    )
