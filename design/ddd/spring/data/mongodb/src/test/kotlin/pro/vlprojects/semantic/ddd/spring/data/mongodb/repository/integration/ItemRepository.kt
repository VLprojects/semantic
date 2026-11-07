package pro.vlprojects.semantic.ddd.spring.data.mongodb.repository.integration

import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Item
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.ItemId
import pro.vlprojects.semantic.ddd.spring.data.mongodb.repository.MongoEntityRepository

interface ItemRepository : MongoEntityRepository<Item, ItemId>
