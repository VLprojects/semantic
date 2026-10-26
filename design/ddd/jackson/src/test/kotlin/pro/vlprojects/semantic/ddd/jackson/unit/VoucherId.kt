package pro.vlprojects.semantic.ddd.jackson.unit

import com.fasterxml.jackson.annotation.JsonCreator
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class VoucherId(value: UUID) : AbstractId<UUID>(value) {

    companion object {

        @JsonCreator
        @JvmStatic
        fun fromCode(code: String) = code
            .removePrefix("V-")
            .let(UUID::fromString)
            .let(::VoucherId)

        @JsonCreator
        @JvmStatic
        fun fromText(text: String) = text
            .let(UUID::fromString)
            .let(::VoucherId)
    }
}
