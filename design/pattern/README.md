# semantic-pattern

Classic patterns of objects for Kotlin and Java, grouped as the Gang of Four groups
them: creational, structural, behavioral, plus the patterns of Patterns of Enterprise
Application Architecture by Fowler in a group of our own, enterprise. A package per group,
a package per pattern inside it. Plain Kotlin, no framework; adapters come as modules of their own.

In place so far: a registry with its store, a registrar and a factory in the creational group;
the contracts of Composite in the structural one; a resolver in the behavioral one; the contracts
of Plugin in the enterprise one.

## Packages

Packages of `semantic-pattern`, under `pro.vlprojects.semantic.pattern`:

| Package | What is there |
| --- | --- |
| `creational.registry` | `Registry`, named instances of one type that the whole application shares and manages at run time; `Registrar`, populates a registry at startup from one source; `RegistryStore`, keeps the instances of a registry; `InMemoryRegistryStore`, a store in memory |
| `creational.registry.base` | `AbstractRegistry`, a registry over a store; `AbstractRegistrar`, a registrar of definitions through the factory of each class of a definition |
| `creational.registry.error` | `InvalidNameException`, `InstanceAlreadyExistsException`, `InstanceNotFoundException`, `DefaultInstanceNotFoundException`; `InstanceRegistrationException` of a registrar |
| `creational.factory` | `Factory`, an instance made of its definition; `Factory.Definition`, the marker of a definition |
| `creational.factory.error` | `InvalidDefinitionException`; `MultipleFactoriesException`, `FactoryNotFoundException` of a registrar |
| `structural.composite` | `Composite`, the base type of a hierarchy whose containers and leaves a client treats alike; `MutableComposite`, a container whose children change after it is made |
| `behavioral.resolver` | `Resolver`, the values found for a context; `ResolvingPolicy`, one value selected of several, `first` and `last`; `PredicateResolver`, the candidates that hold for a context; `TypeNameResolver`, a subtype by its name |
| `behavioral.resolver.error` | `NotResolvedException`, `MultipleResolvedException` |
| `enterprise.configuration` | `PolymorphicConfigurationBinder`, a block of settings to an object of the class the block names; `Definition`, a block as strings: its name, type and arguments; `DefinitionParser`, flat properties to definitions; `DefinitionMapper`, the arguments of a definition to an object of a class |
| `enterprise.configuration.error` | Of a binder, `InvalidConfigurationException`: `UnknownConfigurationException`, `InvalidConfigurationPropertyException`. Of a parser, `DefinitionParsingException`, an `InvalidConfigurationException` too: `MultipleConfigurationsException`, `UnboundConfigurationPropertiesException`. Of a mapper, `DefinitionConversionException` |

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

## Composite

Composite, as the Gang of Four describe it, lets a client treat a container and a leaf
alike: one call on the root reaches every level. Its Component, Leaf and Composite are classes
of one hierarchy, and the library takes only what every hierarchy repeats, the access to children:

| Role | Who plays it | What it holds |
| --- | --- | --- |
| Component | the base type of a hierarchy, it implements `Composite` | the operation of the hierarchy and, through `Composite`, the children |
| Leaf | a class that keeps the default `children` | the operation alone |
| Composite, a container | a class that overrides `children` | its children and the operation over them |

`Composite` names the pattern, not the role: a leaf implements it too.

![A tree of settings and the role of each element in Composite](../../docs/assets/composite-light.svg#gh-light-mode-only)
![A tree of settings and the role of each element in Composite](../../docs/assets/composite-dark.svg#gh-dark-mode-only)

The example of the book, equipment of a computer:

```kotlin
abstract class Equipment : Composite<Equipment> {

    abstract val power: Int
}

class FloppyDisk : Equipment() {

    override val power = 5
}

class Chassis(children: List<Equipment>) : Equipment() {

    override val children = children.toList()

    override val power
        get() = 20 + this.children.sumOf(Equipment::power)
}

val computer = Chassis(listOf(Chassis(listOf(FloppyDisk())), FloppyDisk()))
```

`computer.power` is 50: the client asks the root and knows nothing of the levels below it.

The children are declared on the base type, empty by default, as the book advises in maximizing the
interface of a component: `CreateIterator` of its `Equipment` returns a `NullIterator`, and
`values()` of a Jackson `JsonNode`, a `Collection`, is empty on a value. A client reads `children`
of any element, with no check of its class.

Unlike the book, `add` and `remove` are apart from `Composite`, as `MutableList` is apart from `List`
in Kotlin. A container whose children change implements `MutableComposite`: it overrides `children`
with a `MutableCollection` and gets `add` and `remove` over it; each returns the container for a
chain of calls, as `append` of `StringBuilder` does. `toImmutable()` returns an element of the
hierarchy with the children of that moment, as `toImmutable()` of a `MutableCollection` in Eclipse
Collections does; the container implements it, since only the hierarchy knows its immutable class:

```kotlin
class MutableChassis(children: List<Equipment>) :
    Equipment(),
    MutableComposite<Equipment> {

    override val children = children.toMutableList()

    override val power
        get() = 20 + this.children.sumOf(Equipment::power)

    override fun toImmutable() = Chassis(this.children)
}
```

The copy is shallow, as in Eclipse Collections: a mutable child stays mutable in it. A leaf does not
implement `MutableComposite`, so `add` of a leaf does not compile, where the book makes it fail at
run time. A hierarchy built once, settings for one, takes `Composite` alone, and its container takes
its children when it is made.

`Composite<C : Composite<C>>` is bound by its own type, as `Enum<E : Enum<E>>` is: the children of
a container are of the hierarchy, and the children of a child are read with no cast. The children
are a `Collection`, with no index and no key: a container of settings finds a child by its name, a
list by its index, and each keeps that lookup of its own beside `children`.

`hasChildren()` tells whether an element has children, as `hasChildren` of a JFace
`ITreeContentProvider` and `hasChildNodes()` of a DOM `Node` do: an empty container has none,
as a leaf, and a walk, a search or a fold has nowhere to go from either. Where an empty container
means something of its own, an empty block of settings for one, the hierarchy tells it by an
operation of its classes, not by a flag of the library.

Builder makes such a hierarchy, Iterator walks it, Visitor adds an operation to it without a
change of its classes, Flyweight shares its equal parts. A Decorator of the hierarchy overrides
`children` with those of the element it wraps, or a decorated container reads as a leaf.

![Builder makes the tree of settings from flat keys](../../docs/assets/composite-builder-light.svg#gh-light-mode-only)
![Builder makes the tree of settings from flat keys](../../docs/assets/composite-builder-dark.svg#gh-dark-mode-only)

![Iterator walks the tree in three orders](../../docs/assets/composite-iterator-light.svg#gh-light-mode-only)
![Iterator walks the tree in three orders](../../docs/assets/composite-iterator-dark.svg#gh-dark-mode-only)

![Visitor turns the tree into flat keys](../../docs/assets/composite-visitor-light.svg#gh-light-mode-only)
![Visitor turns the tree into flat keys](../../docs/assets/composite-visitor-dark.svg#gh-dark-mode-only)

![Chain of Responsibility passes a failure from a leaf up to the root](../../docs/assets/composite-chain-light.svg#gh-light-mode-only)
![Chain of Responsibility passes a failure from a leaf up to the root](../../docs/assets/composite-chain-dark.svg#gh-dark-mode-only)

![Flyweight shares equal subtrees](../../docs/assets/composite-flyweight-light.svg#gh-light-mode-only)
![Flyweight shares equal subtrees](../../docs/assets/composite-flyweight-dark.svg#gh-dark-mode-only)

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
an object of the class the block names, is `enterprise.configuration`; Dropwizard calls it polymorphic
configuration. It knows no factory: the class may be any, a definition or not, as Spring Boot
`Binder` binds any class. A registrar may join the halves: its `loadDefinitions` binds each block
by a `PolymorphicConfigurationBinder<D>` and makes an instance by the factory of the class of the definition.

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
with a factory and its definition, and no list of the types has to change.

`PolymorphicConfigurationBinder<T>` binds polymorphic configuration: flat string settings to an
object of the subtype of `T` that the settings name by the first segment of their keys. Spring Boot
`Binder.bind(name, target)` binds to the class the caller names; here the settings name it.
It makes no plugin and knows no factory. The class of a type comes from a resolver of the
behavioral group, a `Resolver<String, KClass<out T>>`:
`TypeNameResolver.of(BackoffDefinition::class, definitions)` names `ExponentialBackoffDefinition`
`exponential`, `TypeNameResolver.of(types)` takes the names it is given. The adapter picks the
resolver.

A block comes as `Map<String, String>` with keys flattened as Spring Boot flattens a tree:
`exponential.multiplier`, `exponential.jitter.factor`, `exponential.retry-on-statuses[0]`.
`prefix` is the prefix of the block, `semantic.resilience.backoff.instances.orders`: its keys
are relative to it, as to the prefix of `@ConfigurationProperties`, and the errors carry it, so
a wrong block is found by its prefix.

The contract of a binder of one block, as it is now; step 3 of the plan turns it into a binder of
all the blocks under a prefix, made of a parser, a resolver and a mapper, and the layout of keys
and the default type move to the parser:

- The type is the first segment of a key. Keys of one block that name different types throw
  `MultipleConfigurationsException` with the prefix of the block and the types.
- A type that the resolver does not find throws `UnknownConfigurationException` with the prefix
  of the block and the type, as Jackson throws `InvalidTypeIdException`. A field outside a type,
  `multiplier=3`, is a value on a type key and throws `UnboundConfigurationPropertiesException`.
- A resolver that finds several classes for a type throws `MultipleResolvedException` of its own;
  `TypeNameResolver` never does, as its names are unique.
- An empty block takes the default type the binder is made with. A default type the resolver does
  not find fails when the binder is made, with `IllegalArgumentException`.
- Types are compared as written: `fixed-delay` and `fixedDelay` in one block throw
  `MultipleConfigurationsException`, though `TypeNameResolver` matches both to one class.
- Fields that do not fit the class, a required one missing, an extra one or a wrong value, throw
  `InvalidConfigurationPropertyException` with the prefix and the `DefinitionConversionException` of
  the mapper as its cause.

Every error of a binder is an `InvalidConfigurationException`: the configuration of the block is
invalid, and the caller catches one exception. The block is an argument of `bind`, so it is an
`IllegalArgumentException`, as `NumberFormatException` is for a wrong number and
`MultipleFactoriesException` is here. Fields are the business of the adapter: the one of Spring
Boot is to bind with `NoUnboundElementsBindHandler` and wrap its `BindException`.

A binder has no defaults of its own: a block is bound alone. A default instance is the business of
a registry, `Registry.resolveOrDefault`; settings shared by blocks, as `base-config` of
Resilience4j, may come as a layer above the binder when a module needs them.

The binder asks the resolver by `resolveOrNull` and throws itself: only the binder knows the
prefix of the block. Jackson splits it the same way: `TypeNameIdResolver` returns `null`,
`TypeDeserializerBase` throws.

A definition checks its fields in `init`: the binder of Spring Boot then fails with the path of
the block. A factory that rejects a definition loses that path, since a registrar knows only
the name of the instance.

A type in a field of the block, `type: exponential` as `PROPERTY` of Jackson, is not supported:
no module needs it yet.

### Definition, parser, mapper

Spring Boot flattens the settings of a module to keys of four parts:

```
semantic.resilience.backoff.instances . orders . exponential . initial-delay = 200ms
prefix                                  name     type          argument
```

The prefix is where a module keeps its blocks, with the section `instances` when the module has
one, as `resilience4j.retry.instances` of Resilience4j does. The name is the name of an instance,
the type picks a class, and the rest of the key is an argument: `jitter.factor` and
`retry-on-statuses[0]` too.

Binding splits in three, as Jackson splits a polymorphic read into `TypeDeserializer`,
`TypeIdResolver` and `JsonDeserializer`:

| Part | What it does | In Jackson |
| --- | --- | --- |
| `DefinitionParser` | flat properties to a `Definition` per block: the name, the type and the arguments | `TypeDeserializer` finds the type id |
| `Resolver<String, KClass<out T>>` | a type to a class | `TypeIdResolver` |
| `DefinitionMapper` | the arguments of a definition to an object of a class | `JsonDeserializer` |

`Definition` is a block as strings, before any class, as `FilterDefinition(name, args)`
of Spring Cloud Gateway is. It is not a `Factory.Definition`: a mapper makes one of it. Its
`toString` names the keys of the arguments and hides their values, which may be secrets.

The type is a string, as the type id of Jackson is: a short name, `exponential`, or a name of a
class, as a resolver reads it. A definition always has a type, and none of its name, type and
argument keys is blank. It copies the arguments, so a change of the given map does not reach it.

The contract of `DefinitionParser`, named as `BeanDefinitionParser` of Spring: `parse(properties)`
takes all the flat properties of an application and gives one definition per name of a block it
finds, in no order. Where the blocks are and how their keys are laid out is up to the parser: it is
made with what it needs, as `AsPropertyTypeDeserializer` of Jackson is made with the name of the
type property and the default type. Its errors are `DefinitionParsingException`, as
`BeanDefinitionParsingException` is of Spring, with the prefix of the block: keys of one block that
name different types throw `MultipleConfigurationsException`, keys it cannot read throw
`UnboundConfigurationPropertiesException` with the keys, as Spring Boot does.

The first parser, of named blocks, is to be made with a prefix and a default type, and to read named
definitions of flat properties under the prefix, as `PropertiesBeanDefinitionReader` of Spring does:

- The keys outside the prefix are left out. The prefix ends at `.` or `[`:
  `semantic.resilience.backoff.instancesBackup.orders` is outside `semantic.resilience.backoff.instances`.
- The name is the first segment after the prefix. A name in brackets, `instances[orders.v2]`, is
  `orders.v2`, as Spring Boot keeps a map key with a dot. The type is the next segment, up to `.`
  or `[`. The rest of the key after the `.` is an argument.
- A block that names no type, `orders=` as Spring Boot flattens `orders:` of YAML, takes the default
  type, as `TypeDeserializerBase` of Jackson takes `defaultImpl`. A parser made with no default type
  leaves the key unbound.
- An empty value on the type key, `orders.fixed=`, names the type and gives no argument: Spring Boot
  4.1 flattens `fixed:` and `fixed: {}` of YAML so; 4.0 drops `fixed: {}`, and the block takes the
  default type.
- Keys of one block that name different types throw `MultipleConfigurationsException` with the
  prefix of the block, `semantic.resilience.backoff.instances.orders`, and the types. Types are
  compared as written: a parser knows no classes, and a map of flat keys keeps no order of their
  sources, so neither of `fixed-delay` and `fixedDelay` may win. An environment variable spells a
  type with no dash, `ORDERS_FIXEDDELAY_DELAY` as `orders.fixeddelay.delay`: a block it overrides
  names its type `fixeddelay` in YAML too.
- A key that is neither an argument nor one of those two, a value on them, `orders=5` or
  `orders.fixed=5`, or a list in their place, `orders[0]=fixed` or `orders.fixed[0]=`, is left
  unbound and throws `UnboundConfigurationPropertiesException` with the prefix of the block.

The contract of `DefinitionMapper`: `convert(definition, targetType)` makes an object of
`targetType` of the arguments, as `ObjectMapper.convertValue(map, type)` of Jackson does. No
arguments give the defaults of the class; a required argument missing, an extra one or a wrong value
throws `DefinitionConversionException` with the definition and the target type, as
`ConversionService` of Spring throws `ConversionFailedException` with its `targetType`, so the
binder wraps no other error. The class is chosen before: a mapper reads no type, as a
`JsonDeserializer` of Jackson does not; the name of the definition is there for its errors. The core
has no mapper, as it is plain Kotlin: the Spring Boot adapter is to give one over `Binder`, wrapping
its `BindException`. The definition is an argument of `convert`, so its error is an
`IllegalArgumentException`, as the one of `convertValue` is. A mapper knows no prefix, so its error
is not an `InvalidConfigurationException`: the binder wraps it in one.

Two layouts are not supported, as no module needs them yet: a type in a field of the block,
`type: exponential` as `PROPERTY` of Jackson, and a list of blocks with the name in a field,
`backoff[0].name=orders` as `routes[0].id` of Spring Cloud Gateway. Either is a parser of its own.

### Plan of Plugin

1. The contract of `PolymorphicConfigurationBinder`, its errors.
2. `Definition`; the contracts of `DefinitionParser` and `DefinitionMapper`.
3. A binder of all the blocks under a prefix: `bind` gives the objects by the names of the blocks,
   the `Map<String, D>` that `loadDefinitions` of a registrar returns.
4. A parser of named blocks, made with a prefix and a default type.
5. `GenericConfigurationBinder`: a parser, a resolver and a mapper.
6. A Spring Boot adapter, `semantic-pattern-spring-boot`: a mapper over the `Binder` of Spring Boot.
7. Backoff and the polling of delivery moved onto it.

## Plan

1. The contracts of `Registry`, `Registrar`, `Factory` and `RegistryStore`; `AbstractRegistry`
   and `InMemoryRegistryStore`.
2. Closing the instances of a registry on shutdown.
3. A registry to read apart from one to change; `replace` of an expected instance.
4. Metrics of a registry, kept in step with its changes without events.
5. A Spring Boot adapter: a registrar over settings, taken out of backoff.
