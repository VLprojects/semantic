package pro.vlprojects.semantic.ddd.spring.data.unit

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class ItemId(
    value: Long,
) : AbstractId<Long>(value)

open class Item(
    override val identifier: ItemId,
) : AbstractSpringDataEntity<Item, ItemId>() {

    fun loaded() = apply { markPersisted() }
}

class Bundle(
    override val identifier: ItemId,
) : AbstractSpringDataEntity<Bundle, ItemId>()

// ProxyUtils tells a CGLIB proxy by the $$ in the name of its class.
@Suppress("ktlint:standard:class-naming")
class `Item$$Proxy`(
    identifier: ItemId,
) : Item(identifier)
