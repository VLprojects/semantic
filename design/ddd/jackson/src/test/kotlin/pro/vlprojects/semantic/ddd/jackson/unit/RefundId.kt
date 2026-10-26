package pro.vlprojects.semantic.ddd.jackson.unit

import com.fasterxml.jackson.annotation.JsonCreator
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class RefundId
    @JsonCreator(mode = JsonCreator.Mode.DISABLED)
    constructor(value: UUID) : AbstractId<UUID>(value)
