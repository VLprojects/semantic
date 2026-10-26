package pro.vlprojects.semantic.ddd.jackson

import tools.jackson.databind.DeserializationContext

internal typealias IdentifierCreator = (DeserializationContext, Any) -> Any?
