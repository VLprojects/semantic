package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit.config

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.support.StaticListableBeanFactory
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffBeansRegistrar

class BackoffBeansRegistrarTests {

    private val bean = FixedBackoff.ofSeconds(9)
    private val beanFactory = StaticListableBeanFactory()
    private val registry = BackoffRegistry()

    @Test
    fun `a bean with the name of a registered backoff replaces it`() {
        registry.register("payments", FixedBackoff.ofSeconds(1))
        beanFactory.addBean("payments", bean)

        BackoffBeansRegistrar(beanFactory).register(registry)

        assertSame(bean, registry["payments"])
    }

    @Test
    fun `a bean with a free name is added`() {
        beanFactory.addBean("orders", bean)

        BackoffBeansRegistrar(beanFactory).register(registry)

        assertSame(bean, registry["orders"])
    }

    @Test
    fun `a bean named default replaces the default`() {
        registry.register(BackoffRegistry.DEFAULT_NAME, FixedBackoff.ofSeconds(1))
        beanFactory.addBean(BackoffRegistry.DEFAULT_NAME, bean)

        BackoffBeansRegistrar(beanFactory).register(registry)

        assertSame(bean, registry.default)
    }
}
