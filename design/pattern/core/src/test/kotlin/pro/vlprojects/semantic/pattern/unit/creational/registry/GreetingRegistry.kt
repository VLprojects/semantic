package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.AbstractRegistry
import pro.vlprojects.semantic.pattern.creational.registry.InMemoryRegistryStore

class GreetingRegistry : AbstractRegistry<String>(InMemoryRegistryStore())
