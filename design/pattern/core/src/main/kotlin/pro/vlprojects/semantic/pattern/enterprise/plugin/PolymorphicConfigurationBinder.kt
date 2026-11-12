package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationException

/**
 * Binds polymorphic configuration: flat string settings to an object of the subtype of [T]
 * that the settings name by the first segment of their keys.
 */
public interface PolymorphicConfigurationBinder<out T : Any> {

    /**
     * Binds the block at the path [name]; [defaults] supply what the block omits, its type included.
     */
    @Throws(InvalidConfigurationException::class)
    public fun bind(
        name: String,
        properties: Map<String, String>,
        defaults: Map<String, String> = emptyMap(),
    ): T
}
