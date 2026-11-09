package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.beans.factory.ListableBeanFactory
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry

internal class BackoffBeansRegistrar(private val beanFactory: ListableBeanFactory) : BackoffRegistrar {

    override fun register(registry: BackoffRegistry) = beanFactory
        .getBeansOfType(Backoff::class.java)
        .forEach(registry::replace)
}
