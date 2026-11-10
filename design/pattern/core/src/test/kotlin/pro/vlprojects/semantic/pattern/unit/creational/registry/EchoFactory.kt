package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.factory.Factory

class EchoFactory<D : Factory.Definition> : Factory<D, String> {

    override fun create(definition: D) = definition.toString()
}
