package pro.vlprojects.semantic.pattern.enterprise.configuration

import pro.vlprojects.semantic.pattern.enterprise.configuration.error.InvalidConfigurationException

public interface DefinitionParser {

    /**
     * Parses the flat [properties] of an application to a definition per block it finds;
     * where the blocks are and how their keys are laid out is up to the parser.
     */
    @Throws(InvalidConfigurationException::class)
    public fun parse(properties: Map<String, String>): List<Definition>
}
