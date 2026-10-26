package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import tools.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(keyUsing = DockIdKeyDeserializer::class)
class DockId(value: Long) : AbstractId<Long>(value)
