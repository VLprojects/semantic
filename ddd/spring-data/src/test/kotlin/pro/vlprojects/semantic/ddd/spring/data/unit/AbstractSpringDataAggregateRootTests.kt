package pro.vlprojects.semantic.ddd.spring.data.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import org.springframework.data.util.AnnotationDetectionMethodCallback
import org.springframework.util.ReflectionUtils

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
