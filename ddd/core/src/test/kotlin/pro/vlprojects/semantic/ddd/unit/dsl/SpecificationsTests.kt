package pro.vlprojects.semantic.ddd.unit.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.dsl.allOf
import pro.vlprojects.semantic.ddd.dsl.and
import pro.vlprojects.semantic.ddd.dsl.anyOf
import pro.vlprojects.semantic.ddd.dsl.check
import pro.vlprojects.semantic.ddd.dsl.not
import pro.vlprojects.semantic.ddd.dsl.remainderUnsatisfiedBy
import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.AllOf
import pro.vlprojects.semantic.ddd.tactical.specification.Always
import pro.vlprojects.semantic.ddd.tactical.specification.Never
import pro.vlprojects.semantic.ddd.unit.tactical.base.ContainsSku
import pro.vlprojects.semantic.ddd.unit.tactical.base.IsOpen
import pro.vlprojects.semantic.ddd.unit.tactical.base.NonEmpty
import pro.vlprojects.semantic.ddd.unit.tactical.base.OfCustomer
import pro.vlprojects.semantic.ddd.unit.tactical.base.Order
import pro.vlprojects.semantic.ddd.unit.tactical.base.OrderStatus
import pro.vlprojects.semantic.ddd.unit.tactical.base.TotalAtLeast
import pro.vlprojects.semantic.ddd.unit.tactical.base.order

class SpecificationsTests {

    private val open = order(status = OrderStatus.OPEN, total = 50)
    private val paid = order(status = OrderStatus.PAID, total = 150)

    @Test
    fun `and holds when both hold`() {
        val discount = IsOpen and TotalAtLeast(100)

        assertTrue(discount.isSatisfiedBy(order(total = 100)))
        assertFalse(discount.isSatisfiedBy(open))
        assertFalse(discount.isSatisfiedBy(paid))
    }

    @Test
    fun `anyOf holds when one holds`() {
        val either = anyOf(IsOpen, TotalAtLeast(100))

        assertTrue(either.isSatisfiedBy(open))
        assertTrue(either.isSatisfiedBy(paid))
        assertFalse(either.isSatisfiedBy(order(status = OrderStatus.PAID, total = 50)))
    }

    @Test
    fun `not inverts`() {
        assertFalse((!IsOpen).isSatisfiedBy(open))
        assertTrue((!IsOpen).isSatisfiedBy(paid))
    }

    @Test
    fun `nested conjunctions are flattened`() {
        val flat = (IsOpen and TotalAtLeast(100)) and OfCustomer("ann")

        assertEquals(3, (flat as AllOf).specifications.size)
    }

    @Test
    fun `neutral elements are dropped and absorbing ones win`() {
        assertEquals(IsOpen, IsOpen and Always)
        assertEquals(Never, IsOpen and Never)
        assertEquals(IsOpen, anyOf(IsOpen, Never))
        assertEquals(Always, anyOf(IsOpen, Always))
    }

    @Test
    fun `empty combinations are the neutral elements`() {
        assertEquals(Always, emptyList<Specification<Order>>().allOf())
        assertEquals(Never, emptyList<Specification<Order>>().anyOf())
    }

    @Test
    fun `a single specification stands for itself`() {
        assertEquals(IsOpen, allOf(IsOpen))
        assertEquals(IsOpen, anyOf(IsOpen))
    }

    @Test
    fun `double negation cancels out`() {
        assertEquals(IsOpen, !!IsOpen)
        assertEquals(Never, !Always)
        assertEquals(Always, !Never)
    }

    @Test
    fun `combinations are equal regardless of the order`() {
        assertEquals(IsOpen and TotalAtLeast(100), TotalAtLeast(100) and IsOpen)
        assertEquals((IsOpen and TotalAtLeast(100)).hashCode(), (TotalAtLeast(100) and IsOpen).hashCode())
        assertEquals(anyOf(IsOpen, ContainsSku("a")), anyOf(ContainsSku("a"), IsOpen))
    }

    @Test
    fun `the remainder is what the candidate does not satisfy`() {
        val discount = IsOpen and TotalAtLeast(100) and OfCustomer("ann")

        assertEquals(TotalAtLeast(100), discount.remainderUnsatisfiedBy(open))
        assertEquals(Always, discount.remainderUnsatisfiedBy(order(total = 100)))
        assertEquals(IsOpen and TotalAtLeast(100), discount.remainderUnsatisfiedBy(order(OrderStatus.PAID, 50)))
    }

    @Test
    fun `a specification filters a collection`() {
        val orders = listOf(open, paid)

        assertEquals(listOf(open), orders.filter(IsOpen))
        assertEquals(1, orders.count(!IsOpen))
    }

    @Test
    fun `a specification of its own checks any object`() {
        assertTrue(NonEmpty.isSatisfiedBy(listOf(1)))
        assertFalse(NonEmpty.isSatisfiedBy(emptySet<Int>()))
    }

    @Test
    fun `a specification makes a business rule`() {
        val discount = rule(IsOpen and TotalAtLeast(100)) { "No discount for $total" }

        val failure = assertThrows<BusinessRuleException> { discount.check(open) }

        assertEquals("No discount for 50", failure.message)
    }

    @Test
    fun `a combination is written by its parts`() {
        assertEquals("(IsOpen and not TotalAtLeast(amount=100))", (IsOpen and !TotalAtLeast(100)).toString())
    }
}
