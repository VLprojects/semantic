package pro.vlprojects.semantic.ddd.spring.data.jpa.repository.integration

import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Item
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.ItemId
import pro.vlprojects.semantic.ddd.spring.data.jpa.repository.JpaEntityRepository

interface ItemRepository : JpaEntityRepository<Item, ItemId>
