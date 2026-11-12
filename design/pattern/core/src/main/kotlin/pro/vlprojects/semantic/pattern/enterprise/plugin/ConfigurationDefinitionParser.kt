package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationException

public interface ConfigurationDefinitionParser {

    /**
     * Parses the keys of [properties] under [prefix] to a definition per name that follows [prefix];
     * the keys outside [prefix] are left out.
     */
    @Throws(InvalidConfigurationException::class)
    public fun parse(prefix: String, properties: Map<String, String>): List<ConfigurationDefinition>
}
