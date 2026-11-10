package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory

object MorningFactory : Factory<MorningDefinition, String> {

    override val definitionType = MorningDefinition::class

    override fun create(definition: MorningDefinition) = "${definition.text}!"
}
