package pro.vlprojects.semantic.pattern.creational.registry

import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException

public fun interface Registrar<T : Any> {

    @Throws(InstanceRegistrationException::class)
    public fun populate(registry: Registry<T>)
}
