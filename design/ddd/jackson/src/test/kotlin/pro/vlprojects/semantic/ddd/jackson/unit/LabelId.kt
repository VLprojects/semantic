package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class LabelId(value: UUID) : AbstractId<UUID>(value) {

    companion object {

        @JvmStatic
        fun fromString(text: String) = text
            .removePrefix("LBL-")
            .let(UUID::fromString)
            .let(::LabelId)
    }
}
