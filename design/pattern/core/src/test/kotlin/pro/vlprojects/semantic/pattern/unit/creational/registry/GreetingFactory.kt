package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidDefinitionException

object GreetingFactory : Factory<GreetingDefinition, String> {

    override fun supports(definition: Factory.Definition) = definition is GreetingDefinition

    override fun create(definition: GreetingDefinition) = definition.text
        .takeIf(String::isNotBlank)
        ?: throw InvalidDefinitionException("A greeting is blank")
}
