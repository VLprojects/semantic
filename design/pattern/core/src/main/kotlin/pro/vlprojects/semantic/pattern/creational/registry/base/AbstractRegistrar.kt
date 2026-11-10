package pro.vlprojects.semantic.pattern.creational.registry.base

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.factory.error.FactoryNotFoundException
import pro.vlprojects.semantic.pattern.creational.factory.error.MultipleFactoriesException
import pro.vlprojects.semantic.pattern.creational.factory.resolveDefinitionType
import pro.vlprojects.semantic.pattern.creational.registry.Registrar
import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException

public abstract class AbstractRegistrar<D : Factory.Definition, T : Any>(
    factories: List<Factory<*, T>>,
) : Registrar<T> {

    private val factoriesByDefinitionType = factories
        .groupBy(Factory<*, T>::resolveDefinitionType)
        .mapValues { (definitionType, candidates) ->
            candidates.singleOrNull()
                ?: throw MultipleFactoriesException(definitionType)
        }

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

    @Suppress("UNCHECKED_CAST") // factories are keyed by the class of the definition they take
    private fun resolveFactory(definition: D) = factoriesByDefinitionType[definition.javaClass] as Factory<D, T>?
        ?: throw FactoryNotFoundException(definition)
}
