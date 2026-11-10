# semantic-pattern

Classic patterns of objects for Kotlin and Java, grouped as the Gang of Four groups
them: creational, structural, behavioral. A package per group, a package per
pattern inside it. Plain Kotlin, no framework; adapters come as modules of their own.

Only the creational group is in place so far: a registry with its store, a registrar, a factory.

## Packages

Packages of `semantic-pattern`, under `pro.vlprojects.semantic.pattern`:

| Package | What is there |
| --- | --- |
| `creational.registry` | `Registry`, named instances of one type that the whole application shares and manages at run time; `Registrar`, populates a registry at startup from one source; `RegistryStore`, keeps the instances of a registry; `InMemoryRegistryStore`, a store in memory |
| `creational.registry.base` | `AbstractRegistry`, a registry over a store |
| `creational.registry.error` | `InvalidNameException`, `InstanceAlreadyExistsException`, `InstanceNotFoundException`, `DefaultInstanceNotFoundException`; `InstanceRegistrationException` of a registrar |
| `creational.factory` | `Factory`, an instance made of its definition; `Definition`, the marker of a definition |
| `creational.factory.error` | `InvalidDefinitionException` |

`structural` and `behavioral` get packages when a pattern of them is needed.

## Registry, registrar, factory, store

Four roles, each with one reason to change:

| Role | What it does | What it does not | Samples |
| --- | --- | --- | --- |
| `Registry<T>` | keeps instances by name and finds them; replaces and removes them at run time | builds nothing, knows no factory and no settings | Spring `SingletonBeanRegistry`, Spring Boot `SslBundleRegistry`, Resilience4j `Registry` |
| `Registrar<T>` | populates a registry once at startup from one source: settings, annotations, beans, code | hands out no instance, nobody asks it | Spring `BeanRegistrar`, Spring Boot `SslBundleRegistrar`; `populate` as in Spring Data `RepositoryPopulator` and Spring JDBC `DatabasePopulator` |
| `Factory<D, T>` | makes an instance of its definition | knows no name | Spring AMQP `RabbitListenerContainerFactory`, the factories of backoff |
| `RegistryStore<T>` | keeps the instances of a registry: `find`, `putIfAbsent`, `getAndReplace`, `getAndRemove`, each atomic, and `getAll` | checks no name, throws no exception of the contract | Resilience4j `RegistryStore`; the methods as in JCache `Cache` |

A definition is the data an instance is made of, the delays of an exponential
backoff for one; it carries no name, as a Spring `BeanDefinition` does not. It
implements `Definition`, an empty interface that a factory is bound by and a module
may extend, as Spring marks its callbacks with `Aware`. The
factories are dependencies of a registrar, not entries of a registry: their set
is fixed when the application is assembled.

```kotlin
class BackoffPropertiesRegistrar(
    private val properties: BackoffProperties,
    private val factory: Factory<BackoffDefinition, Backoff>,
) : Registrar<Backoff> {

    override fun populate(registry: Registry<Backoff>) {
        properties.instances.forEach { (name, definition) ->
            try {
                registry.register(name, factory.create(definition))
            } catch (failure: RuntimeException) {
                throw InstanceRegistrationException(name, failure)
            }
        }
    }
}
```

The code of an application looks an instance up, `registry["orders"]`; the
registry is injected, never reached through a static, so it is no Service Locator.

## The contract of a registry

- `register` of a blank name throws `InvalidNameException`.
- A name is registered once: `register` of a taken name throws `InstanceAlreadyExistsException`,
  as `registerMBean` of JMX throws its `InstanceAlreadyExistsException`.
- `replace`, `unregister` and `get` of an unknown name throw `InstanceNotFoundException`, as
  `unregisterMBean` of JMX does; `find` returns `null`.
- `replace` and `unregister` return the instance they took out, for its owner to close it,
  as `replace` in Resilience4j and `unregisterContributor` in Spring Boot do.
- `getAll` returns a snapshot of the instances, as `getAllCircuitBreakers` in Resilience4j
  does; later changes do not touch it.
- The default instance is the one under `Registry.DEFAULT_NAME`, `"default"`, as the default
  configuration of Resilience4j is. A registrar puts it as any other.
- `getOrDefault` falls back to the default instance for an unknown name, or throws
  `DefaultInstanceNotFoundException`, an `InstanceNotFoundException`, when there is none.
  `get` never falls back, so a misspelt name fails where nobody asked for a fallback.

Each exception of a registry extends the one of the JDK a caller expects: `IllegalArgumentException`,
`IllegalStateException`, `NoSuchElementException`. It carries the name that failed.

A registry checks a name and turns the answers of its store into the exceptions above:
`putIfAbsent` that returns `false` means a taken name, `getAndReplace` or `getAndRemove`
that returns `null` means an unknown one. `AbstractRegistry` does it once for every store, as
`AbstractRegistry` of Resilience4j does over its `RegistryStore`. `InMemoryRegistryStore`
keeps instances in a `ConcurrentHashMap`; a cache or a database fits a store too, for an
instance that can be serialized.

```kotlin
class BackoffRegistry(store: RegistryStore<Backoff>) : AbstractRegistry<Backoff>(store)
```

A registrar makes every instance at startup, not on its first lookup: a wrong
definition stops the startup instead of failing a call later, as a singleton in Spring
is made eagerly unless it is marked lazy. A factory rejects a wrong definition with
`InvalidDefinitionException`, as Jackson does with its own; a factory may extend it.

A registrar wraps any failure of an instance in `InstanceRegistrationException` with the
name it failed on and the failure as its cause: a wrong definition, a blank name, a taken
one. A caller of `populate` catches one exception and learns which instance broke the
startup, as `MBeanRegistrationException` of JMX wraps a failure of registration.

Registrars run in their order. A name that two of them take stops the startup, unless
the later one replaces it on purpose: a bean from code overrides a setting by `replace`.

## Plan

1. The contracts of `Registry`, `Registrar`, `Factory` and `RegistryStore`; `AbstractRegistry`
   and `InMemoryRegistryStore`.
2. Metrics of a registry, kept in step with its changes without events.
3. A Spring Boot adapter: a registrar over settings, taken out of backoff.
