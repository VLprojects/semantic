package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class BadgeId(value: String) : AbstractId<String>(value) {

    init {
        value.startsWith("B-").let { isBadge -> require(isBadge) { "A badge id starts with B-" } }
    }
}
