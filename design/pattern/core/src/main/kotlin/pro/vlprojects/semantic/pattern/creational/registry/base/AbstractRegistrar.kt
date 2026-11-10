package pro.vlprojects.semantic.pattern.creational.registry.base

import pro.vlprojects.semantic.pattern.creational.factory.Factory
import pro.vlprojects.semantic.pattern.creational.factory.error.FactoryNotFoundException
import pro.vlprojects.semantic.pattern.creational.factory.error.MultipleFactoriesException
import pro.vlprojects.semantic.pattern.creational.registry.Registrar
import pro.vlprojects.semantic.pattern.creational.registry.Registry
import pro.vlprojects.semantic.pattern.creational.registry.error.InstanceRegistrationException

public abstract class AbstractRegistrar<D : Factory.Definition, T : Any>(
    factories: List<Factory<*, T>>,
) : Registrar<T> {

    private val factories = factories
        .groupBy { factory -> factory.definitionType.java }
        .mapValues { (definitionType, candidates) ->
            candidates.singleOrNull()
                ?: throw MultipleFactoriesException(definitionType.kotlin)
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

    private fun resolveFactory(definition: D) = definition.javaClass
        .let(::findNearestFactory)
        ?.let(::cast)
        ?: throw FactoryNotFoundException(definition)

    private fun findNearestFactory(definitionType: Class<*>) = definitionType
        .let(::listOf)
        .let(::readSupertypesByDistance)
        .map { supertypes -> supertypes.mapNotNull(factories::get) }
        .firstOrNull(List<Factory<*, T>>::isNotEmpty)
        ?.let { candidates ->
            candidates.singleOrNull()
                ?: throw MultipleFactoriesException(definitionType.kotlin)
        }

    private fun readSupertypesByDistance(types: List<Class<*>>) = generateSequence(types) { nearer ->
        nearer
            .flatMap(::readSupertypes)
            .distinct()
            .ifEmpty { null }
    }

    private fun readSupertypes(type: Class<*>) = type.interfaces
        .asSequence()
        .plus(type.superclass)
        .filterNotNull()
        .toList()

    @Suppress("UNCHECKED_CAST") // a factory of a supertype of D takes D as well
    private fun cast(factory: Factory<*, T>) = factory as Factory<D, T>
}
