package pro.vlprojects.semantic.ddd.spring.data.unit

import pro.vlprojects.semantic.ddd.spring.data.AbstractSpringDataEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class ItemId(
    value: Long,
) : AbstractId<Long>(value)

class Item(
    identifier: ItemId,
) : AbstractSpringDataEntity<Item, ItemId>() {

    init {
        this.identifier = identifier
    }

    override fun isNew() = true
}
