package pro.vlprojects.semantic.ddd.spring.data.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import org.springframework.data.util.AnnotationDetectionMethodCallback
import org.springframework.util.ReflectionUtils
import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class AbstractSpringDataAggregateRootTests {

    @Test
    fun `the id is the identifier`() {
        assertEquals(CartId(1), Cart(CartId(1)).id)
    }

    @Test
    fun `spring data finds the events to publish`() {
        val cart = Cart(CartId(1)).add("apple").add("pear")

        assertEquals(listOf("apple", "pear"), cart.call<DomainEvents>().map { (it as ItemAdded).sku })
    }

    @Test
    fun `the events handed out do not follow later changes`() {
        val cart = Cart(CartId(1)).add("apple")
        val events = cart.call<DomainEvents>()

        cart.add("pear")

        assertEquals(1, events.size)
    }

    @Test
    fun `an event names the revision it happened at`() {
        val event = Cart(CartId(1), version = 3).add("apple").call<DomainEvents>().single()

        assertEquals(BaseRevision("cart", CartId(1), 3), (event as ItemAdded).aggregateRevision)
    }

    @Test
    fun `a change that breaks an invariant throws`() {
        val cart = Cart(CartId(1)).add("apple").add("pear")

        val failure = assertThrows<BusinessRuleException> { cart.add("plum") }

        assertEquals("Cart 1 holds more than 2 items", failure.message)
    }

    @Test
    fun `an aggregate root shows its class, kind and identifier`() {
        assertEquals("Cart [aggregate-root][id: 1]", Cart(CartId(1)).toString())
    }

    @Test
    fun `published events are forgotten`() {
        val cart = Cart(CartId(1)).add("apple")

        cart.call<AfterDomainEventPublication>()

        assertEquals(emptyList<Any>(), cart.call<DomainEvents>())
    }

    // The same lookup EventPublishingRepositoryProxyPostProcessor makes before and after a save.
    private inline fun <reified T : Annotation> Any.call() = AnnotationDetectionMethodCallback(T::class.java)
        .also { ReflectionUtils.doWithMethods(javaClass, it) }
        .invoke<Collection<Any>>(this)
        .orEmpty()
}
