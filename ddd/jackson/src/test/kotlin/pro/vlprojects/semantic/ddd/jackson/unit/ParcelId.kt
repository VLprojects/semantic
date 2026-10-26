package pro.vlprojects.semantic.ddd.jackson.unit

import com.fasterxml.jackson.annotation.JsonCreator
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class ParcelId private constructor(value: UUID) : AbstractId<UUID>(value) {

    companion object {

        @JsonCreator
        @JvmStatic
        fun create(value: UUID) = ParcelId(value)
    }
}
