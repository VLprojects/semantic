package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form

internal class BackoffTypeRegistry private constructor(
    private val types: Map<ConfigurationPropertyName, BackoffType<*>>,
) {

    val names = types.keys.map { name -> name.getLastElement(Form.ORIGINAL) }

    fun find(name: ConfigurationPropertyName) = types[name]

    companion object Factory {

        fun of(factories: Map<String, BackoffFactory<*>>): BackoffTypeRegistry {
            val types = factories.mapValues { (beanName, factory) -> BackoffType.of(beanName, factory) }

            types.entries
                .groupBy({ (_, type) -> type.name }, { (beanName) -> beanName })
                .forEach { (name, beanNames) -> requireSingleBean(name, beanNames) }

            return types.values
                .associateBy { type -> type.name }
                .let(::BackoffTypeRegistry)
        }

        private fun requireSingleBean(name: ConfigurationPropertyName, beanNames: List<String>) =
            require(beanNames.size == 1) { "Several backoff factory beans give the type '$name': $beanNames" }
    }
}
