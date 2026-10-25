package pro.vlprojects.semantic.ddd.spring.data.unit

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class ItemId(
    value: Long,
) : AbstractId<Long>(value)

open class Item(
    identifier: ItemId,
) : AbstractSpringDataEntity<Item, ItemId>() {

    init {
        this.identifier = identifier
    }

    fun loaded() = apply { markPersisted() }
}

// ProxyUtils tells a CGLIB proxy by the $$ in the name of its class.
@Suppress("ktlint:standard:class-naming")
class `Item$$Proxy`(
    identifier: ItemId,
) : Item(identifier)
