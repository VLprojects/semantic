package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class OrderId(value: UUID) : AbstractId<UUID>(value)
