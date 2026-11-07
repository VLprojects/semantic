package pro.vlprojects.semantic.ddd.spring.data.mongodb.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.AfterDomainEventPublication
import org.springframework.data.domain.DomainEvents
import org.springframework.data.util.AnnotationDetectionMethodCallback
import org.springframework.util.ReflectionUtils
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.Cart
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.CartId
import pro.vlprojects.semantic.ddd.spring.data.mongodb.integration.ItemAdded
import pro.vlprojects.semantic.ddd.tactical.base.BaseRevision

class AbstractMongoAggregateRootTests {

    @Test
    fun `Spring Data finds the registered events`() {
        val event = Cart(CartId(1))
            .add()
            .invokeAnnotated<DomainEvents>()
            .single() as ItemAdded
        val expected = BaseRevision("cart", CartId(1), 0)

        assertEquals(expected, event.aggregateRevision)
    }

    @Test
    fun `published events are forgotten`() {
        val cart = Cart(CartId(1)).add()

        cart.invokeAnnotated<AfterDomainEventPublication>()

        assertEquals(emptyList<Any>(), cart.invokeAnnotated<DomainEvents>())
    }

    @Test
    fun `an aggregate root shows its class, role and identifier`() {
        assertEquals("Cart [aggregate-root][id: 1]", Cart(CartId(1)).toString())
    }

    // The same lookup EventPublishingRepositoryProxyPostProcessor makes before and after a save.
    private inline fun <reified T : Annotation> Any.invokeAnnotated() = AnnotationDetectionMethodCallback(T::class.java)
        .also { ReflectionUtils.doWithMethods(this.javaClass, it) }
        .invoke<Collection<Any>>(this)
        .orEmpty()
}
