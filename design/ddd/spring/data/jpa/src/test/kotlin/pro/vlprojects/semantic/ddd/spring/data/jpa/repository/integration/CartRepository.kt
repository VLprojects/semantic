package pro.vlprojects.semantic.ddd.spring.data.jpa.repository.integration

import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Cart
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.CartId
import pro.vlprojects.semantic.ddd.spring.data.jpa.repository.JpaAggregateRepository

interface CartRepository : JpaAggregateRepository<Cart, CartId>
