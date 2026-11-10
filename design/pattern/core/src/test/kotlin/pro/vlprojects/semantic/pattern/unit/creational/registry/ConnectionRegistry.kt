package pro.vlprojects.semantic.pattern.unit.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.InMemoryRegistryStore
import pro.vlprojects.semantic.pattern.creational.registry.base.AbstractRegistry

class ConnectionRegistry : AbstractRegistry<Connection>(InMemoryRegistryStore())
