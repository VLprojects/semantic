package pro.vlprojects.semantic.pattern.unit.creational.registry

object ShoutFactory : TextFactory<ShoutDefinition>() {

    override fun create(definition: ShoutDefinition) = definition.text.uppercase()
}
