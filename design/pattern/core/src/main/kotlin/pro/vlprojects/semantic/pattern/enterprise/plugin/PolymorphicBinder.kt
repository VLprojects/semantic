package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.TypeNotFoundException

/**
 * Binds a block of settings to an object of the class the block names by its key: the configuration half
 * of Plugin, polymorphic configuration in the terms of Dropwizard.
 *
 * ```yaml
 * orders:
 *   exponential:
 *     multiplier: 3
 * ```
 *
 * The block `orders` binds to the class found for the type `exponential`, so a module adds a type with
 * no list of the types to change. A binder knows neither where the settings come from nor which
 * factory makes a plugin of the object; a registrar may join both halves.
 */
public interface PolymorphicBinder<out T : Any> {

    /**
     * Binds the block at the path [name] to an object of its type.
     *
     * Keys come flattened as Spring Boot flattens a tree, `exponential.jitter.factor`, and the first
     * segment of a key names the type. [defaults] fill the fields the block does not set and give the
     * type to an empty block; their keys of another type are left out. The errors carry [name], so
     * a wrong block is found by its path; the fields are checked by the implementation.
     */
    @Throws(TypeNotFoundException::class, MultipleTypesException::class)
    public fun bind(
        name: String,
        properties: Map<String, String>,
        defaults: Map<String, String> = emptyMap(),
    ): T
}
