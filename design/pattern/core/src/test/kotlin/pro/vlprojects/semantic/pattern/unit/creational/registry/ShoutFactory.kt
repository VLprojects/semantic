package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory

object ShoutFactory : Factory<ShoutDefinition, String> {

    override fun supports(definition: Factory.Definition) = definition is ShoutDefinition

    override fun create(definition: ShoutDefinition) = definition.text.uppercase()
}
