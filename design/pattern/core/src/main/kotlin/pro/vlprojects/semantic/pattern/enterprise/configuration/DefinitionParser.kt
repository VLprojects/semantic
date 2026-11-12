package pro.vlprojects.semantic.pattern.enterprise.configuration

import pro.vlprojects.semantic.pattern.enterprise.configuration.error.DefinitionParsingException

public interface DefinitionParser {

    /**
     * Parses the flat [properties] of an application to one definition per name of a block it finds;
     * where the blocks are and how their keys are laid out is up to the parser.
     */
    @Throws(DefinitionParsingException::class)
    public fun parse(properties: Map<String, String>): List<Definition>
}
