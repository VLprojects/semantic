package pro.vlprojects.semantic.pattern.creational.factory.error

import kotlin.reflect.KClass

public class MultipleFactoriesException(
    definitionType: KClass<*>,
) : IllegalArgumentException("Several factories create an instance of ${definitionType.java.name}")
