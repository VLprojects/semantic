# semantic-pattern

Classic patterns of objects for Kotlin and Java, grouped as the Gang of Four groups
them: creational, structural, behavioral. A package per group, a package per
pattern inside it. Plain Kotlin, no framework; adapters come as modules of their own.

In place so far: a registry with its store, a registrar and a factory in the creational group;
a resolver in the behavioral one.

## Packages

Packages of `semantic-pattern`, under `pro.vlprojects.semantic.pattern`:

| Package | What is there |
| --- | --- |
| `creational.registry` | `Registry`, named instances of one type that the whole application shares and manages at run time; `Registrar`, populates a registry at startup from one source; `RegistryStore`, keeps the instances of a registry; `InMemoryRegistryStore`, a store in memory |
| `creational.registry.base` | `AbstractRegistry`, a registry over a store; `AbstractRegistrar`, a registrar of definitions through the factory of each class of a definition |
| `creational.registry.error` | `InvalidNameException`, `InstanceAlreadyExistsException`, `InstanceNotFoundException`, `DefaultInstanceNotFoundException`; `InstanceRegistrationException` of a registrar |
| `creational.factory` | `Factory`, an instance made of its definition; `Factory.Definition`, the marker of a definition |
| `creational.factory.error` | `InvalidDefinitionException`; `MultipleFactoriesException`, `FactoryNotFoundException` of a registrar |
| `behavioral.resolver` | `Resolver`, the values found for a context; `ResolvingPolicy`, one value selected of several, `first` and `last`; `PredicateResolver`, the candidates that hold for a context; `TypeNameResolver`, a subtype by its name; `ClassNameResolver`, a subtype by the full name of its class |
| `behavioral.resolver.error` | `NotResolvedException`, `MultipleResolvedException`; `ClassNotAllowedException`, `NotSubtypeException` of a class name resolver |

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

An implementation may reject a context it does not take: every method, `resolveAll` included,
throws an `IllegalArgumentException` then, as `ClassNameResolver` rejects a class outside its
allowed packages.

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

`ClassNameResolver` finds a subtype by the full name of its class, such as
`type: com.acme.backoff.JitteredBackoff` in settings, as `ClassNameIdResolver` of Jackson finds
a class by its name in JSON. It suits subtypes not known in advance, where `TypeNameResolver`
takes their list. A class loaded by a name from outside is a known vulnerability, the one of
polymorphic deserialization in Jackson, so the resolver loads only what its allowed packages
hold and returns only its base type or a subtype of it:

- a name outside the allowed packages is rejected by `ClassNotAllowedException` before the
  class is loaded, as `BasicPolymorphicTypeValidator` of Jackson checks the name of a
  subclass first by `allowIfSubType` and `DefaultJacksonJavaTypeMapper` of Spring Kafka by its
  trusted packages; a package allows its subpackages, as `basePackages` of Spring component
  scanning does;
- a class is loaded without running its static initializers, as `ClassUtils.forName` of
  Spring loads it, by one class loader, the first of:
  1. the one given to `of`;
  2. the one that loaded `ClassNameResolver`, where it sees the base type, as
     `ClassUtils.isVisible` of Spring checks a class: on a flat class path or in a Spring Boot
     fat jar a base type of the JDK, as `Any` or `DataSource`, then still finds the classes of
     the application;
  3. the one of the base type;
  4. the system one.

  A class this loader does not see resolves nothing, where `TypeFactory.findClass` of Jackson
  tries the next loader. The context class loader of the thread is not asked yet: under
  Spring Boot DevTools, pass `Thread.currentThread().contextClassLoader` to `of`;
- a nested class is named by its binary name, `com.acme.Outer$Inner`, as `Class.forName`
  takes it;
- a class that is missing resolves nothing, as Jackson leaves an unknown class to the caller;
- a class that cannot be loaded, as a name that differs in case only on a file system that
  ignores case, is rejected by an `IllegalArgumentException` with the `LinkageError` as its
  cause, as `ClassUtils.resolveClassName` of Spring rejects a class definition it cannot
  resolve;
- a class that is not a subtype is rejected by `NotSubtypeException`, as Jackson rejects it.

An empty set of allowed packages, or an allowed package that is not dot-separated Java
identifiers (`Character.isJavaIdentifierStart`, `isJavaIdentifierPart`), such as `com.acme.`,
`1com` or `*`, fails `of` with an `IllegalArgumentException` instead of silently allowing none,
where `*` of Spring Kafka trusts every class.

```kotlin
val types = ClassNameResolver.of(Backoff::class, setOf("com.acme"))
val type = types.resolve("com.acme.backoff.JitteredBackoff")
```

## Plan

1. The contracts of `Registry`, `Registrar`, `Factory` and `RegistryStore`; `AbstractRegistry`
   and `InMemoryRegistryStore`.
2. Closing the instances of a registry on shutdown.
3. A registry to read apart from one to change; `replace` of an expected instance.
4. Metrics of a registry, kept in step with its changes without events.
5. A Spring Boot adapter: a registrar over settings, taken out of backoff.
