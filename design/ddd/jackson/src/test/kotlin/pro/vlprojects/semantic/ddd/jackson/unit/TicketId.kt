package pro.vlprojects.semantic.ddd.jackson.unit

import com.fasterxml.jackson.annotation.JsonValue
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class TicketId(value: UUID) : AbstractId<UUID>(value) {

    @get:JsonValue(false)
    override val value get() = super.value

    @get:JsonValue
    val code get() = "TCK-$value"
}
