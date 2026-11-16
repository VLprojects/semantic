package pro.vlprojects.semantic.pattern.structural.decorator

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationAwareOrderComparator

public class DecoratorFactoryBeanPostProcessor(
    private val beanFactory: ConfigurableListableBeanFactory,
) : BeanPostProcessor {

    private val targetTypesByFactoryName by lazy(::resolveTargetTypes)

    private val factoryComparator = AnnotationAwareOrderComparator.INSTANCE.withSourceProvider(::findOrderSource)

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val factories = this.targetTypesByFactoryName
            .filterValues { targetType -> targetType.isInstance(bean) }
            .keys
            .sortedWith(this.factoryComparator)
            .map(::getFactory)

        return factories.foldRight(bean) { factory, target -> factory.decorate(target) }
    }

    // the type of a factory comes from its definition, so a factory of another type is not made here
    private fun resolveTargetTypes() = this.beanFactory
        .getBeanNamesForType(DecoratorFactory::class.java, false, false)
        .associateWith(::resolveTargetType)

    private fun resolveTargetType(factoryName: String) = this.beanFactory
        .getMergedBeanDefinition(factoryName)
        .resolvableType
        .`as`(DecoratorFactory::class.java)
        .resolveGeneric()
        ?: error("Decorator factory '$factoryName' must declare its type as DecoratorFactory<T>")

    // the sources of an order are those of FactoryAwareOrderSourceProvider of Spring, which is private
    private fun findOrderSource(factoryName: Any): Array<Any> {
        val factory = this.getFactory(factoryName as String)
        val definition = this.beanFactory.getMergedBeanDefinition(factoryName)

        val orderAttribute = definition
            .getAttribute(AbstractBeanDefinition.ORDER_ATTRIBUTE)
            ?.let { order -> order as Int }
            ?.let { order -> Ordered { order } }

        val factoryMethod = (definition as? RootBeanDefinition)?.resolvedFactoryMethod

        return listOfNotNull(orderAttribute, factoryMethod, factory).toTypedArray()
    }

    @Suppress("UNCHECKED_CAST") // a factory is picked for a bean of the type it decorates
    private fun getFactory(factoryName: String) = this.beanFactory
        .getBean(factoryName) as DecoratorFactory<Any>
}
