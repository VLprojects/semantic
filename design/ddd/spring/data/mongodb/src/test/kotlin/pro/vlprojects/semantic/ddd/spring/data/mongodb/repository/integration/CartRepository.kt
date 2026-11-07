package pro.vlprojects.semantic.ddd.spring.data.mongodb.repository.integration

import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Cart
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.CartId
import pro.vlprojects.semantic.ddd.spring.data.mongodb.repository.MongoAggregateRepository

interface CartRepository : MongoAggregateRepository<Cart, CartId>
