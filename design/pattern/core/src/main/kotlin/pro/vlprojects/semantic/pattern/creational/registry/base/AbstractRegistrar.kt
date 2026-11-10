package pro.vlprojects.semantic.pattern.creational.registry.base

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.factory.error.FactoryNotFoundException
import pro.vlprojects.semantic.pattern.creational.factory.error.MultipleFactoriesException
import pro.vlprojects.semantic.pattern.creational.registry.Registrar
import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException

public abstract class AbstractRegistrar<D : Factory.Definition, T : Any>(
    private val factories: List<Factory<*, T>>,
) : Registrar<T> {

    protected abstract fun loadDefinitions(): Map<String, D>

    @Throws(InstanceRegistrationException::class)
    final override fun populate(registry: Registry<T>) {
        loadDefinitions().forEach { (name, definition) ->
            try {
                definition
                    .let(::create)
                    .let { instance -> registry.register(name, instance) }
            } catch (failure: RuntimeException) {
                throw InstanceRegistrationException(name, failure)
            }
        }
    }

    private fun create(definition: D) = definition
        .let(::resolveFactory)
        .create(definition)

    @Suppress("UNCHECKED_CAST") // supports() guarantees the factory takes this definition
    private fun resolveFactory(definition: D): Factory<D, T> {
        val candidates = factories.filter { factory -> factory.supports(definition) }

        return when {
            candidates.isEmpty() -> throw FactoryNotFoundException(definition)
            candidates.size > 1 -> throw MultipleFactoriesException(definition)
            else -> candidates.single() as Factory<D, T>
        }
    }
}
