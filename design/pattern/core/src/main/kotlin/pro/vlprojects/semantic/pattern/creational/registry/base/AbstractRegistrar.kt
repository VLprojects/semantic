package pro.vlprojects.semantic.pattern.creational.registry.base

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.factory.error.FactoryNotFoundException
import pro.vlprojects.semantic.pattern.creational.factory.error.MultipleFactoriesException
import pro.vlprojects.semantic.pattern.creational.registry.Registrar
import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException

public abstract class AbstractRegistrar<D : Factory.Definition, T : Any>(
    factories: List<Factory<out D, T>>,
) : Registrar<T> {

    private val factories = factories
        .groupBy(Factory<out D, T>::definitionType)
        .mapValues { (definitionType, candidates) ->
            candidates
                .singleOrNull()
                ?: throw MultipleFactoriesException(definitionType)
        }

    protected abstract fun loadDefinitions(): Map<String, D>

    @Throws(InstanceRegistrationException::class)
    final override fun populate(registry: Registry<T>): Unit = loadDefinitions()
        .forEach { (name, definition) ->
            runCatching { register(registry, name, definition) }
                .onFailure { failure -> handleFailure(name, failure) }
        }

    private fun register(registry: Registry<T>, name: String, definition: D) = definition
        .let(::resolveFactory)
        .create(definition)
        .let { instance -> registry.register(name, instance) }

    private fun handleFailure(name: String, failure: Throwable): Nothing = when (failure) {
        is Exception -> throw InstanceRegistrationException(name, failure)
        else -> throw failure
    }

    @Suppress("UNCHECKED_CAST") // factories are keyed by the class of the definition they take
    private fun resolveFactory(definition: D) = factories[definition::class] as Factory<D, T>?
        ?: throw FactoryNotFoundException(definition)
}
