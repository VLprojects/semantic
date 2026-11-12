package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationException

/**
 * Binds polymorphic configuration: flat string settings to an object of the subtype of [T]
 * that the settings name by the first segment of their keys.
 */
public interface PolymorphicConfigurationBinder<out T : Any> {

    /**
     * Binds the block of [properties] under [prefix] to an object of the subtype of [T] its keys name.
     */
    @Throws(InvalidConfigurationException::class)
    public fun bind(prefix: String, properties: Map<String, String>): T
}
