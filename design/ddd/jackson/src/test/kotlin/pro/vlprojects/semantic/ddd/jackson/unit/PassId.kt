package pro.vlprojects.semantic.ddd.jackson.unit

import com.fasterxml.jackson.annotation.JsonCreator
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class PassId(value: UUID) : AbstractId<UUID>(value) {

    companion object {

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun parse(text: String) = text
            .removePrefix("PASS-")
            .let(UUID::fromString)
            .let(::PassId)
    }
}
