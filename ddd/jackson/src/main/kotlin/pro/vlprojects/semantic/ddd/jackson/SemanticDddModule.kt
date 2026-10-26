package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.core.Version
import tools.jackson.databind.JacksonModule

public class SemanticDddModule : JacksonModule() {

    override fun getModuleName(): String = "semantic-ddd-jackson"

    override fun version(): Version = Version.unknownVersion()

    override fun setupModule(context: SetupContext) {
        context.appendAnnotationIntrospector(BoxedIdentifierIntrospector)
        context.addKeyDeserializers(BoxedIdentifierKeyDeserializers)
    }
}
