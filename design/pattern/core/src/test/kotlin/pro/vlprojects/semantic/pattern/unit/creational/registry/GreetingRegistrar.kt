package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.base.AbstractRegistrar

class GreetingRegistrar(
    private val definitions: Map<String, GreetingDefinition>,
) : AbstractRegistrar<GreetingDefinition, String>(GreetingFactory) {

    override fun loadDefinitions() = definitions
}
