package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.aopalliance.intercept.MethodInterceptor
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.Ordered

class ProxyingBeanPostProcessor :
    BeanPostProcessor,
    Ordered {

    override fun postProcessAfterInitialization(bean: Any, beanName: String) = when (bean) {
        is VisualComponent -> ProxyFactory(bean)
            .apply { addAdvice(MethodInterceptor { invocation -> "${invocation.proceed()}°" }) }
            .proxy

        else -> bean
    }

    override fun getOrder() = Ordered.LOWEST_PRECEDENCE
}
