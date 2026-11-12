package pro.vlprojects.semantic.pattern.enterprise.configuration

import pro.vlprojects.semantic.pattern.enterprise.configuration.error.InvalidConfigurationException

public interface DefinitionParser {

    /**
     * Parses the keys of [properties] under [prefix] to a definition per name that follows [prefix];
     * the keys outside [prefix] are left out.
     */
    @Throws(InvalidConfigurationException::class)
    public fun parse(prefix: String, properties: Map<String, String>): List<Definition>
}
