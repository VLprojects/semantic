package pro.vlprojects.semantic.pattern.creational.factory.error

public class MultipleFactoriesException(
    definitionType: Class<*>,
) : IllegalArgumentException("Several factories create an instance of ${definitionType.name}")
