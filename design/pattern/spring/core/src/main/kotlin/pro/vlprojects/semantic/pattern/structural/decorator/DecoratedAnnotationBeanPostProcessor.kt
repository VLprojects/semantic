package pro.vlprojects.semantic.pattern.structural.decorator

import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.BeanNotOfRequiredTypeException
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy
import java.util.concurrent.ConcurrentHashMap

public class DecoratedAnnotationBeanPostProcessor(
    private val beanFactory: AutowireCapableBeanFactory,
) : BeanPostProcessor, DisposableBean {

    private val factories = ConcurrentHashMap<Class<*>, AnnotationDecoratorFactory<Annotation, Any>>()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val targetClass = AopProxyUtils.ultimateTargetClass(bean)
        val annotations = MergedAnnotations
            .from(targetClass, SearchStrategy.DIRECT)
            .filter(MergedAnnotation<Annotation>::isDirectlyPresent)
            .filter(::isDecorated)
            .map(MergedAnnotation<Annotation>::synthesize)

        return annotations.foldRight(bean) { annotation, target -> this.decorate(target, annotation, beanName) }
    }

    override fun destroy() {
        this.factories.values.forEach(this.beanFactory::destroyBean)
    }

    private fun isDecorated(annotation: MergedAnnotation<Annotation>) = annotation.type
        .isAnnotationPresent(Decorated::class.java)

    private fun decorate(target: Any, annotation: Annotation, beanName: String): Any {
        val annotationClass = annotation.annotationClass.java
        val factoryClass = annotationClass
            .getAnnotation(Decorated::class.java)
            .value
            .java
        val (annotationType, targetType) = ResolvableType
            .forClass(factoryClass)
            .`as`(AnnotationDecoratorFactory::class.java)
            .resolveGenerics(Any::class.java)

        check(annotationType == annotationClass) {
            "@${annotationClass.name} of bean '$beanName' names ${factoryClass.name}, " +
                "which decorates by @${annotationType.name}"
        }
        if (!targetType.isInstance(target)) {
            throw BeanNotOfRequiredTypeException(beanName, targetType, target.javaClass)
        }

        return this
            .findFactory(factoryClass)
            .decorate(target, annotation)
    }

    // createBean may post-process an annotated bean, so it runs outside computeIfAbsent, which forbids recursion
    private fun findFactory(factoryClass: Class<*>) = this.factories[factoryClass]
        ?: factoryClass
            .let(::createFactory)
            .let { factory -> this.factories.putIfAbsent(factoryClass, factory) ?: factory }

    @Suppress("UNCHECKED_CAST") // the generics of the factory are checked against the annotation and the target
    private fun createFactory(factoryClass: Class<*>) = this.beanFactory
        .createBean(factoryClass) as AnnotationDecoratorFactory<Annotation, Any>
}
