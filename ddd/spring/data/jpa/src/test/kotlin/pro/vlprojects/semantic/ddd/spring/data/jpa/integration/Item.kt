package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import pro.vlprojects.semantic.ddd.spring.data.jpa.AbstractJpaEntity
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity

@Embeddable
class ItemId(
    value: Long,
) : LongId(value)

@Entity
class Item(
    identifier: ItemId,
) : AbstractJpaEntity<Item, ItemId>() {

    init {
        this.identifier = identifier
    }
}
