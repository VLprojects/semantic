package pro.vlprojects.semantic.pattern.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceAlreadyExistsException
import pro.vlprojects.semantic.pattern.creational.registry.error.InvalidNameException

public fun interface Registrar<T : Any> {

    @Throws(InvalidNameException::class, InstanceAlreadyExistsException::class)
    public fun populate(registry: Registry<T>)
}
