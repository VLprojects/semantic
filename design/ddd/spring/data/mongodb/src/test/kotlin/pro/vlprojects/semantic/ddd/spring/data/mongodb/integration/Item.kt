package pro.vlprojects.semantic.ddd.spring.data.mongodb.integration

import pro.vlprojects.semantic.ddd.spring.data.mongodb.AbstractMongoEntity
import pro.vlprojects.semantic.ddd.tactical.base.AbstractId

class ItemId(
    value: Long,
) : AbstractId<Long>(value)

class Item(
    identifier: ItemId,
) : AbstractMongoEntity<Item, ItemId>() {

    init {
        this.identifier = identifier
    }
}
