# semantic-pattern

Classic patterns of objects for Kotlin and Java, grouped as the Gang of Four groups
them: creational, structural, behavioral. A package per group, a package per
pattern inside it. Plain Kotlin, no framework; adapters come as modules of their own.

Only the contracts of the creational group are in place so far.

## Packages

Packages of `semantic-pattern`, under `pro.vlprojects.semantic.pattern`:

| Package | What is there |
| --- | --- |
| `creational.registry` | `Registry`, named instances of one type that the whole application shares and manages at run time; `Registrar`, fills a registry at startup from one source |
| `creational.factory` | `Factory`, an instance made of its definition |

`structural` and `behavioral` get packages when a pattern of them is needed.

## Registry, registrar, factory

Three roles, each with one reason to change:

| Role | What it does | What it does not | Samples |
| --- | --- | --- | --- |
| `Registry<T>` | keeps instances by name and finds them; replaces and removes them at run time; creates an instance registered as a supplier on its first lookup | builds nothing on its own, knows no factory and no settings | Spring `SingletonBeanRegistry`, `BeanRegistry`, Spring Boot `SslBundleRegistry`, Resilience4j `Registry` |
| `Registrar<T>` | fills a registry once at startup from one source: settings, annotations, beans, code | hands out no instance, nobody asks it | Spring `BeanRegistrar`, `FormatterRegistrar`, Spring Boot `SslBundleRegistrar` |
| `Factory<D, T>` | makes an instance of its definition | knows no name | Spring AMQP `RabbitListenerContainerFactory`, the factories of backoff |

A definition is the data an instance is made of, the delays of an exponential
backoff for one; it carries no name, as a Spring `BeanDefinition` does not. The
factories are dependencies of a registrar, not entries of a registry: their set
is fixed when the application is assembled.

```kotlin
class BackoffPropertiesRegistrar(
    private val properties: BackoffProperties,
    private val factory: Factory<BackoffDefinition, Backoff>,
) : Registrar<Backoff> {

    override fun register(registry: Registry<Backoff>) {
        properties.instances.forEach { (name, definition) ->
            registry.register(name) { factory.create(definition) }
        }
    }
}
```

The code of an application looks an instance up, `registry["orders"]`; the
registry is injected, never reached through a static, so it is no Service Locator.

## The contract of a registry

- A name is not blank, or `IllegalArgumentException`.
- A name is registered once: `register` of a taken name throws `IllegalStateException`,
  as `registerSingleton` in Spring does. `replace` and `unregister` of an unknown name throw
  `NoSuchElementException`, as `removeBeanDefinition` throws `NoSuchBeanDefinitionException`.
- `get` of an unknown name throws `NoSuchElementException`, `find` returns `null`.
- An instance registered as a supplier is made on the first `find` or `get`, once, even
  when threads ask for it together, as a lazy singleton in Spring is. `names` and
  `contains` make nothing.
- `names` keep the order of registration, so logs and actuator stay stable.

Registrars run in their order. A name that two of them take stops the startup, unless
the later one replaces it on purpose: a bean from code overrides a setting by `replace`.

## Plan

1. The contracts of `Registry`, `Registrar` and `Factory`.
2. `InMemoryRegistry`: copy on write under a monitor, a supplier called once.
3. Events of a registry, an instance registered, replaced or unregistered, for metrics.
4. A Spring Boot adapter: a registrar over settings, taken out of backoff.
