package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.registry.base.AbstractRegistrar

class GreetingRegistrar(
    private val definitions: Map<String, Factory.Definition>,
    factories: List<Factory<*, String>> = listOf(GreetingFactory, ShoutFactory),
) : AbstractRegistrar<Factory.Definition, String>(factories) {

    override fun loadDefinitions() = definitions
}
