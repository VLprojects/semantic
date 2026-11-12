package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.TypeNotFoundException

/**
 * Binds flat string settings to a typed object whose class is not known in advance but chosen by the settings
 * themselves, by the key of the block: `exponential.multiplier=3`.
 */
public interface PolymorphicConfigurationBinder<out T : Any> {

    /**
     * Binds the block at the path [name]; [defaults] fill what the block does not set, its type included.
     */
    @Throws(TypeNotFoundException::class, MultipleTypesException::class)
    public fun bind(
        name: String,
        properties: Map<String, String>,
        defaults: Map<String, String> = emptyMap(),
    ): T
}
