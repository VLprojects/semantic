package pro.vlprojects.semantic.ddd.spring.data.unit.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.core.div
import org.springframework.data.domain.Sort
import pro.vlprojects.semantic.ddd.spring.data.repository.order
import pro.vlprojects.semantic.ddd.spring.data.repository.sort
import pro.vlprojects.semantic.ddd.spring.data.unit.Cart
import pro.vlprojects.semantic.ddd.spring.data.unit.CartId

class SortExtensionsTests {

    @Test
    fun `a property sorts ascending by its name`() {
        assertEquals(Sort.by("version"), Cart::version.sort())
    }

    @Test
    fun `a property of the base class sorts by its name`() {
        assertEquals(Sort.by("identifier"), Cart::identifier.sort())
    }

    @Test
    fun `a nested property sorts by its dot path`() {
        assertEquals(Sort.by("identifier.value"), (Cart::identifier / CartId::value).sort())
    }

    @Test
    fun `a property orders ascending by its name`() {
        assertEquals(Sort.Order.by("version"), Cart::version.order())
    }

    @Test
    fun `sorts of properties join in order`() {
        assertEquals(
            Sort.by(Sort.Order.desc("version"), Sort.Order.asc("identifier")),
            Cart::version.sort().descending().and(Cart::identifier.sort()),
        )
    }
}
