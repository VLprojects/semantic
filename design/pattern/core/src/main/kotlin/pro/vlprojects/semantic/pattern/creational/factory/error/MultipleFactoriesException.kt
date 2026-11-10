package pro.vlprojects.semantic.pattern.creational.factory.error

import pro.vlprojects.semantic.pattern.creational.factory.Factory

public class MultipleFactoriesException(
    definition: Factory.Definition,
) : IllegalStateException("Several factories support a definition of ${definition::class.simpleName}")
