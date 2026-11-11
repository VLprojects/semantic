# semantic-pattern

Classic patterns of objects for Kotlin and Java, grouped as the Gang of Four groups
them: creational, structural, behavioral, plus the patterns of Patterns of Enterprise
Application Architecture by Fowler in a group of our own, enterprise. A package per group,
a package per pattern inside it. Plain Kotlin, no framework; adapters come as modules of their own.

In place so far: a registry with its store, a registrar and a factory in the creational group; a resolver
in the behavioral one; the contracts of Plugin and its base binder in the enterprise one.

## Packages

Packages of `semantic-pattern`, under `pro.vlprojects.semantic.pattern`:

| Package | What is there |
| --- | --- |
| `creational.registry` | `Registry`, named instances of one type that the whole application shares and manages at run time; `Registrar`, populates a registry at startup from one source; `RegistryStore`, keeps the instances of a registry; `InMemoryRegistryStore`, a store in memory |
| `creational.registry.base` | `AbstractRegistry`, a registry over a store; `AbstractRegistrar`, a registrar of definitions through the factory of each class of a definition |
| `creational.registry.error` | `InvalidNameException`, `InstanceAlreadyExistsException`, `InstanceNotFoundException`, `DefaultInstanceNotFoundException`; `InstanceRegistrationException` of a registrar |
| `creational.factory` | `Factory`, an instance made of its definition; `Factory.Definition`, the marker of a definition |
| `creational.factory.error` | `InvalidDefinitionException`; `MultipleFactoriesException`, `FactoryNotFoundException` of a registrar |
| `behavioral.resolver` | `Resolver`, the values found for a context; `ResolvingPolicy`, one value selected of several, `first` and `last`; `PredicateResolver`, the candidates that hold for a context; `TypeNameResolver`, a subtype by its name |
| `behavioral.resolver.error` | `NotResolvedException`, `MultipleResolvedException` |
| `enterprise.plugin` | `TypeNameResolver`, a class by the name of its type; `PolymorphicBinder`, a block of settings to an object of the class the block names |
| `enterprise.plugin.base` | `AbstractPolymorphicBinder`, a binder that takes the type from the key and leaves the fields to a subclass |
| `enterprise.plugin.error` | `TypeNotFoundException`, `MultipleTypesException` of a binder |

`structural` gets packages when a pattern of it is needed.

## Registry, registrar, factory, store

Four roles, each with one reason to change:

| Role | What it does | What it does not | Samples |
| --- | --- | --- | --- |
| `Registry<T>` | keeps instances by name and finds them; replaces and removes them at run time | builds nothing, knows no factory and no settings | Spring `SingletonBeanRegistry`, Spring Boot `SslBundleRegistry`, Resilience4j `Registry` |
| `Registrar<T>` | populates a registry once at startup from one source: settings, annotations, beans, code | hands out no instance, nobody asks it | Spring `BeanRegistrar`, Spring Boot `SslBundleRegistrar`; `populate` as in Spring Data `RepositoryPopulator` and Spring JDBC `DatabasePopulator` |
| `Factory<D, T>` | names the class of its definition, makes an instance of it | knows no name | Spring AMQP `RabbitListenerContainerFactory`, the factories of backoff |
| `RegistryStore<T>` | keeps the instances of a registry: `find`, `putIfAbsent`, `getAndReplace`, `getAndRemove`, each atomic, `getAndRemoveAll` and `getAll` | checks no name, throws no exception of the contract | Resilience4j `RegistryStore`; the methods as in JCache `Cache` |

A definition is the data an instance is made of, the delays of an exponential
backoff for one; it carries no name, as a Spring `BeanDefinition` does not. It
implements `Factory.Definition`, an empty interface that a factory is bound by and a module
may extend, as Spring marks its callbacks with `Aware`.

A factory names the class of its definition by `definitionType`, a `KClass<D>`, as a deserializer of
Jackson names its class by `handledType` and a `FactoryBean` of Spring by `getObjectType`;
the compiler checks that the class is `D`. A registrar takes a list of factories of its own definitions,
`Factory<out D, T>`: a factory of another definition does not compile. It keys
each by that class: a lookup is one access by the class of a definition, with no pass over
the factories. The constructor of a registrar checks the list: two factories of one
definition throw `MultipleFactoriesException`.

One factory makes one class of a definition, and a lookup takes the exact class, as
`SimpleDeserializers` of Jackson does: a factory of a supertype would read only the fields
of the supertype and lose the rest. A hierarchy of definitions, a sealed `BackoffDefinition`
for one, gives each class a factory of its own. A definition with no factory of its exact
class throws `FactoryNotFoundException` when it is made, even if its supertype has one. The factories are dependencies of a
registrar, not entries of a registry: their set is fixed when the application is assembled.

```kotlin
class BackoffPropertiesRegistrar(
    private val properties: BackoffProperties,
    factories: List<Factory<out BackoffDefinition, Backoff>>,
) : AbstractRegistrar<BackoffDefinition, Backoff>(factories) {

    override fun loadDefinitions() = properties.instances
}
```

The code of an application looks an instance up, `registry.resolve("orders")`; the
registry is injected, never reached through a static, so it is no Service Locator.

## The contract of a registry

- `register` of a blank name throws `InvalidNameException`.
- A name is registered once: `register` of a taken name throws `InstanceAlreadyExistsException`,
  as `registerMBean` of JMX throws its `InstanceAlreadyExistsException`.
- `replace`, `unregister` and `resolve` of an unknown name throw `InstanceNotFoundException`, as
  `unregisterMBean` of JMX does; `find` returns `null`.
- `replace` and `unregister` return the instance they took out, for its owner to close it,
  as `replace` in Resilience4j and `unregisterContributor` in Spring Boot do.
- `getAll` returns a snapshot of the instances, as `getAllCircuitBreakers` in Resilience4j
  does; later changes do not touch it.
- The default instance is the one under `Registry.DEFAULT_NAME`, `"default"`, as the default
  configuration of Resilience4j is. A registrar puts it as any other.
- `resolveDefault` returns the default instance or throws `DefaultInstanceNotFoundException`,
  an `InstanceNotFoundException`. `resolveOrDefault` falls back to it for an unknown name;
  `resolve` never does, so a misspelt name fails where nobody asked for a fallback.
- A lookup is `resolve`, as `DestinationResolver` of Spring resolves a destination by its
  name and throws when it cannot.
- A registry is `AutoCloseable`: `close` takes every instance out and closes each one that is
  `AutoCloseable`, as `close` of a `CacheManager` in JCache closes its caches and Spring
  destroys its singletons on shutdown. A failure of one instance does not stop the others:
  `close` throws the first failure with the rest suppressed, as try-with-resources of the JDK
  does. The registry takes new instances after it, as Spring does after `destroySingletons`.

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
one, a definition with no factory. `AbstractRegistrar` does it once: its `populate` makes and registers every definition
that `loadDefinitions` returns, as `RepositoryBeanDefinitionRegistrarSupport` of Spring Data
leaves its subclass only the annotation and the extension. A registrar of ready beans from
code needs no definitions and implements `Registrar` itself. A caller of `populate` catches one exception and learns which instance broke the
startup, as `MBeanRegistrationException` of JMX wraps a failure of registration.

Registrars run in their order. A name that two of them take stops the startup, unless
the later one replaces it on purpose: a bean from code overrides a setting by `replace`.

## Resolver

A resolver finds the values for a context: a class for the name of a type, a handler for
a message. An implementation writes one method, `resolveAll`, and the rest come with the
contract, as Spring `ObjectProvider` and CDI `Instance` give one, an optional one or all:

| Method | Nothing found | One found | Several found |
| --- | --- | --- | --- |
| `resolveAll(context)` | an empty collection | it | all of them |
| `resolveOrNull(context)` | `null` | it | `MultipleResolvedException` |
| `resolve(context)` | `NotResolvedException` | it | `MultipleResolvedException` |
| `resolveOrNull(context, policy)` | `null` | it | the one the policy selects |
| `resolve(context, policy)` | `NotResolvedException` | it | the one the policy selects |

A policy is asked only when several values are found, as Spring picks a primary bean only
among several candidates. `ResolvingPolicy.first()` and `last()` keep the order `resolveAll`
returns; each is one instance for every type, as `naturalOrder()` of Kotlin is.

```kotlin
val plugin = resolver.resolve(type, ResolvingPolicy.first())
```

`PredicateResolver` keeps a list of candidates and resolves those that hold for a context, in
the order of the list, as `HandlerMethodArgumentResolverComposite` of Spring asks each
resolver whether it `supportsParameter` and `ProviderManager` asks each provider whether it
`supports` an authentication:

```kotlin
val deliverers = PredicateResolver(deliverers, OutboxDeliverer<*>::supports)
val deliverer = deliverers.resolve(destination, ResolvingPolicy.first())
```

`TypeNameResolver` finds a subtype by its name, as the type of a block in settings or a
`type` field of JSON names it. A subtype is named by its class without the name of the base
type at its end: `FixedDelayBackoff` of `Backoff` is `FixedDelay`, and `Backoff` keeps its whole
name. Spring Cloud Gateway names a filter factory alike by `NameUtils.normalizeFilterFactoryName`,
but it removes the name of the base type anywhere in the class name.
`of(types)` takes the names as they are given. A name is matched regardless of case, dashes and
underscores, as Spring Boot relaxed binding matches `fixedDelay`, `fixed-delay` and
`fixed_delay`. Names that coincide regardless of case, dashes and underscores, a name that is
blank but for dashes and underscores, or an anonymous class stop the resolver from being made.

```kotlin
val types = TypeNameResolver.of(Backoff::class, listOf(FixedBackoff::class, ExponentialBackoff::class))
val type = types.resolve("exponential")
```

## Plugin

Plugin, as Fowler describes it, links an interface to its implementation when the application
is configured, not when it is compiled. Here it has two halves. The factory half, a definition to an
instance, is `Factory` and `AbstractRegistrar` above. The configuration half, a block of settings to
an object of the class the block names, is `enterprise.plugin`; Dropwizard calls it polymorphic
configuration. It knows no factory: the class may be any, a definition or not, as Spring Boot
`Binder` binds any class. A registrar joins the halves: it binds a block to a definition of its
own, `PolymorphicBinder<D>`, and makes an instance of it by the factory of its class.

Dropwizard names the type in a field, `type:`; here a block names its type by its key, as
`WRAPPER_OBJECT` of Jackson does:

```yaml
semantic.resilience.backoff.instances:
  orders:
    exponential:
      multiplier: 3
      initial-delay: 200ms
```

The block `orders` is an exponential backoff: `exponential` is its type, `multiplier` and
`initial-delay` are the fields of its definition. The set of types is open: a module adds a type
with a factory and its definition, and no list of the types has to change. The adapter names the
type; the one of Spring Boot takes the name of the bean of the factory without its suffix,
`exponentialBackoffFactory` to `exponential`, as Spring Boot names its health contributors.

| Role | What it does | What it does not | Samples |
| --- | --- | --- | --- |
| `TypeNameResolver<T>` | finds a class by the name of its type, `find`; names the known types, `typeNames` | reads no settings, throws nothing | Jackson `TypeIdResolver`: `typeFromId`, `getDescForKnownTypeIds`; the name as `@JsonTypeName` and `TypeNameIdResolver` |
| `PolymorphicBinder<T>` | binds a block of settings to an object of the class the block names, `bind(name, properties, defaults)` | makes no instance, knows no factory and no definition | Spring Boot `Binder.bind(name, target)` |

A block comes as `Map<String, String>` with keys flattened as Spring Boot flattens a tree:
`exponential.multiplier`, `exponential.jitter.factor`, `exponential.retry-on-statuses[0]`.
`name` is the path of the block, `semantic.resilience.backoff.instances[orders]`: the errors
carry it, so a wrong block is found by its path. `defaults` fills the fields a block does not set,
the default backoff for one, as the default properties of Spring Boot fill what no other
source sets.

The contract of a binder:

- The type is the first segment of a key. Keys of one block that name different types throw
  `MultipleTypesException` with the path of the block and the types.
- A type that the resolver does not find throws `TypeNotFoundException` with the path of the block,
  the type and the known types, as `InvalidTypeIdException` of Jackson lists the known type ids.
  A field outside a type, `orders.multiplier`, names the unknown type `multiplier`.
- An empty block takes the type of `defaults`, and with `defaults` empty too the default type the
  binder is made with.
- Keys of `defaults` of another type than the block are left out. The caller binds `defaults`
  first, as a block of its own under its own path, so its errors carry that path.
- Types are compared by the class the resolver finds, not by their spelling: the relaxed names of
  Spring Boot, `fixed-delay` and `fixedDelay`, stay in the resolver of its adapter.

`TypeNotFoundException` is a `NoSuchElementException`, as `FactoryNotFoundException` is;
`MultipleTypesException` is an `IllegalArgumentException`, as `MultipleFactoriesException` is.
Fields are the business of the adapter: the one of Spring Boot binds with
`NoUnboundElementsBindHandler`, so a field the class does not have throws `BindException`.

A resolver returns `null` for an unknown type, as `Registry.find` does, and the binder throws:
only the binder knows the path of the block. Jackson splits it the same way: `TypeNameIdResolver`
returns `null`, `TypeDeserializerBase` throws.

A definition checks its fields in `init`: the binder of Spring Boot then fails with the path of
the block. A factory that rejects a definition loses that path, since a registrar knows only
the name of the instance.

A type in a field of the block, `type: exponential` as `PROPERTY` of Jackson, is not supported:
no module needs it yet.

### Plan of Plugin

1. The contracts of `TypeNameResolver` and `PolymorphicBinder`, their errors.
2. `AbstractPolymorphicBinder`: the type from the key, one type in a block, `defaults`,
   the default type; a subclass binds only the fields.
3. A Spring Boot adapter, `semantic-pattern-spring-boot`: a resolver over the beans of factories,
   a binder over the `Binder` of Spring Boot.
4. Backoff and the polling of delivery moved onto it.

## Plan

1. The contracts of `Registry`, `Registrar`, `Factory` and `RegistryStore`; `AbstractRegistry`
   and `InMemoryRegistryStore`.
2. Closing the instances of a registry on shutdown.
3. A registry to read apart from one to change; `replace` of an expected instance.
4. Metrics of a registry, kept in step with its changes without events.
5. A Spring Boot adapter: a registrar over settings, taken out of backoff.
