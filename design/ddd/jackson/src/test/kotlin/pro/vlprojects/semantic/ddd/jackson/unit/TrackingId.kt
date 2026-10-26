package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId
import java.util.UUID

class TrackingId(value: UUID) : AbstractId<UUID>(value) {

    init {
        value.version().let { version -> require(version == 7) { "A tracking id is a UUID of version 7" } }
    }
}
