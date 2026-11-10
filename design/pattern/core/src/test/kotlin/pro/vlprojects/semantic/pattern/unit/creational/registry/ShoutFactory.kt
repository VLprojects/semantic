package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory

object ShoutFactory : Factory<ShoutDefinition, String> {

    override val definitionType = ShoutDefinition::class

    override fun create(definition: ShoutDefinition) = definition.text.uppercase()
}
