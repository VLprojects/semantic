package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.factory.error.InvalidDefinitionException

object GreetingFactory : Factory<GreetingDefinition, String> {

    override val definitionType = GreetingDefinition::class

    override fun create(definition: GreetingDefinition) = definition.text
        .takeIf(String::isNotBlank)
        ?: throw InvalidDefinitionException("A greeting is blank")
}
