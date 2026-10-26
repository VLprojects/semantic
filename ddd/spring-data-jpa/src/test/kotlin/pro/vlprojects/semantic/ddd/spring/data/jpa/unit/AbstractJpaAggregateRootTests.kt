package pro.vlprojects.semantic.ddd.spring.data.jpa.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import org.springframework.data.util.AnnotationDetectionMethodCallback
import org.springframework.util.ReflectionUtils
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.Cart
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.CartId
import pro.vlprojects.semantic.ddd.spring.data.jpa.integration.ItemAdded
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class AbstractJpaAggregateRootTests {

    @Test
    fun `Spring Data finds the registered events`() {
        val event = Cart(CartId(1)).add().call<DomainEvents>().single()

        assertEquals(BaseRevision("cart", CartId(1), 0), (event as ItemAdded).aggregateRevision)
    }

    @Test
    fun `published events are forgotten`() {
        val cart = Cart(CartId(1)).add()

        cart.call<AfterDomainEventPublication>()

        assertEquals(emptyList<Any>(), cart.call<DomainEvents>())
    }

    @Test
    fun `an aggregate root shows its class, kind and identifier`() {
        assertEquals("Cart [aggregate-root][id: 1]", Cart(CartId(1)).toString())
    }

    // The same lookup EventPublishingRepositoryProxyPostProcessor makes before and after a save.
    private inline fun <reified T : Annotation> Any.call() = AnnotationDetectionMethodCallback(T::class.java)
        .also { ReflectionUtils.doWithMethods(javaClass, it) }
        .invoke<Collection<Any>>(this)
        .orEmpty()
}
