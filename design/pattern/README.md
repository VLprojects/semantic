# semantic-pattern

Classic patterns of objects for Kotlin and Java, grouped as the Gang of Four groups
them: creational, structural, behavioral, plus the patterns of Patterns of Enterprise
Application Architecture by Fowler in a group of our own, enterprise. A package per group,
a package per pattern inside it. Plain Kotlin, no framework; adapters come as modules of their own:
`semantic-pattern-spring` for Spring.

In place so far: a registry with its store, a registrar and a factory in the creational group;
the contracts of Composite, Decorator and Proxy in the structural one; a resolver and the base types of Visitor in the
behavioral one; the contracts of Plugin in the enterprise one.

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
| `structural.decorator` | `Decorator`, a wrapper that holds its component and adds a responsibility to it; `DecoratorFactory`, a component wrapped in a decorator of its type |
| `structural.proxy` | `Proxy`, a surrogate that stands for its real subject and controls the access to it; `ProxyFactory`, a delegate behind a proxy of its type |
| `behavioral.resolver` | `Resolver`, the values found for a context; `ResolvingPolicy`, one value selected of several, `first` and `last`; `PredicateResolver`, the candidates that hold for a context; `TypeNameResolver`, a subtype by its name |
| `behavioral.resolver.error` | `NotResolvedException`, `MultipleResolvedException` |
| `behavioral.visitor` | `Visitor`, the base type of the visitors of a hierarchy, with `visit`, the method of any element, which each visit method of a hierarchy falls back to; `Visitable`, an element of a hierarchy with `accept` |
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

## Decorator

Decorator, as the Gang of Four describe it, attaches a responsibility to an object at run time: a
decorator has the interface of its component, passes each request to it and adds its own work before
or after. Decorators nest, so a set of responsibilities that is not known when the code is compiled is
put together when the application is assembled. Take a decorator away and its responsibility is gone:
the border of a view, the buffer of a stream, as `BufferedInputStream` is. A wrapper whose removal leaves
the work as it was, a check of rights or a cache, controls the access to the object and is a Proxy.

| Role | Who plays it | What it holds |
| --- | --- | --- |
| Component | an interface of the application | the operations |
| ConcreteComponent | a class that implements it | the work itself |
| Decorator | a class that implements `Decorator` and the interface of its component | `delegate`, the wrapped component, kept private; `delegateClass`, its class; the forwarding of each operation to it |
| ConcreteDecorator | the same class | the operations it overrides, each with its responsibility |

`Decorator<T>` names the role, so a decorator is told from a Proxy by its type, as `SpringProxy` marks a proxy
of Spring AOP. It holds the reference the book calls `_component` as `delegate`, the name CDI gives it with
`@Delegate` and the decorators of Spring give it with `getDelegate()`, `ServerHttpRequestDecorator` of Spring Web
among them. The forwarding of the abstract Decorator of the book is `by` of Kotlin, which writes it for any
interface, from a private property as well. The interface of the component a decorator implements itself: a type
parameter is no supertype, so `Decorator<T>` cannot make a class a `T`. The delegate stays private, as
`_component` of the book does and `delegate()` of Guava `ForwardingObject` is protected: a call through it would
go round the decorator. Only `delegateClass`, the class of the delegate, goes out, as `getTargetClass` of Spring
`TargetClassAware` tells the class of the target of a proxy. In a chain it is the class of the next decorator, as
`getTargetClass` of a proxy around another proxy is.

The example of the book, a text view with a scroll bar and a border:

```kotlin
interface VisualComponent {

    val text: String

    fun draw(): String
}

class ScrollDecorator(private val delegate: VisualComponent) :
    VisualComponent by delegate,
    Decorator<VisualComponent> {

    override val delegateClass = this.delegate::class

    override fun draw() = this.delegate.draw() + "↕"
}

val window = TextView("text")
    .let(::ScrollDecorator)
    .let { scroll -> BorderDecorator(scroll, 1) }
```

`window.draw()` is `|text↕|`, and `text` of the window is the one of the view: `by` forwards it.

Each way to write a decorator in Kotlin has a hole the compiler keeps silent about:

- `by` forwards a member that the interface gains later past the decorator: a decorator of metrics does
  not time it. Java 8 made the same hole with `removeIf`, a default method that `SynchronizedCollection`
  of Commons Collections did not lock, as Effective Java tells in Item 21.
- A decorator without `by` keeps the body that a member has in the interface instead of forwarding it:
  a decorator of a `Composite` reads as a leaf unless it overrides `children`.
- A call that the component makes on itself reaches no decorator: the component does not know of them.

A responsibility for every method of many types, metrics or tracing, fits an interceptor bound to an
annotation better: it gets a method that the interface gains later as well, but sees no types. CDI keeps the
two apart in the same way: a decorator knows what the methods it decorates mean, an interceptor does not.

### Factories of decorators

A factory makes a decorator of a ready component, which comes from outside, so a decorator never makes its
component itself:

```kotlin
val scroll = DecoratorFactory<VisualComponent>(::ScrollDecorator)
```

`DecoratorFactory` is named as `HttpHandlerDecoratorFactory` of Spring Web, a `Function<HttpHandler, HttpHandler>`
whose beans Spring WebFlux applies in their order, and `decorate` as `decorate` of Spring `TaskDecorator` and
`Decorate` of Scrutor. Decorator stays the name of the wrapper, as in the book and as Spring Web keeps
`ServerHttpRequestDecorator` apart from the factory. The compiler checks that a factory returns the type of
its component.

### Spring

`semantic-pattern-spring` decorates a bean by its annotations or by the factories of its type.
`AnnotationDecoratorFactory<A, T>` makes a decorator configured by an annotation of the bean. `Decorated` on an
annotation names its factory, as
`@Constraint(validatedBy = ...)` of Bean Validation names the `ConstraintValidator<A, T>` of a constraint and
`@Conditional` names the condition of `@ConditionalOnClass` in Spring Boot. `DecoratedAnnotationBeanPostProcessor`
wraps a bean by the factories its annotations name, as `DataSourceDecoratorBeanPostProcessor` wraps a `DataSource`
by its decorators. It is named as `ScheduledAnnotationBeanPostProcessor` of Spring, which acts on `@Scheduled`:

```kotlin
@Configuration
@Import(DecoratedAnnotationBeanPostProcessor::class)
class DecoratorConfiguration

@Target(AnnotationTarget.CLASS)
@Decorated(BorderDecoratorFactory::class)
annotation class Bordered(val width: Int)

class BorderDecoratorFactory : AnnotationDecoratorFactory<Bordered, VisualComponent> {

    override fun decorate(target: VisualComponent, annotation: Bordered) =
        BorderDecorator(target, annotation.width)
}

@Target(AnnotationTarget.CLASS)
@Decorated(ScrollDecoratorFactory::class)
annotation class Scrolled

@Bordered(1)
@Scrolled
class TextArea : VisualComponent by TextView("text")
```

The bean of `TextArea` is a `BorderDecorator` around a `ScrollDecorator` around the text area itself:

- The annotations of a class apply in the order they are written: the top one makes the outermost decorator,
  the one a call reaches first, as the top one of stacked decorators of Python and TypeScript does. JUnit
  registers several `@ExtendWith` in the order of the source in the same way. Only the annotations put on the
  class itself count: inherited and composed ones would mix the order. The Javadoc of `getDeclaredAnnotations`
  promises no order, though javac, the Kotlin compiler and HotSpot keep the one of the source, and a test checks it.
- An annotation decorates the bean it is put on, not each bean of a type. It names one factory, so an annotation
  is one decorator: several decorators take several annotations.
- A repeated annotation decorates once per repetition. The compiler packs the repetitions into one container in
  the place of the first one, so they apply together there: write them in a row.
- `Decorated` carries `@Component`, as `@Repository` of Spring does, which marks a bean for
  `PersistenceExceptionTranslationPostProcessor` as well: the scan of components finds a class with a decorating
  annotation without `@Component`. The scan does not look into the container of a repeated annotation, so a class
  with nothing but repetitions takes `@Component` itself. A class with only a composed annotation is found too,
  yet stays bare. A `String` attribute `value` of a decorating annotation would name the bean by a convention that
  Spring deprecates: name it otherwise.
- Only beans are decorated, whatever registers them: the scan, `@Bean` or `@Import`. An object made by its
  constructor is not seen; the module scans no packages, as the application picks its beans itself.
- An annotation is looked up on the class of the bean behind an AOP proxy.
- A factory is made once per its class by `createBean` with its dependencies from the context, as
  `SpringConstraintValidatorFactory` makes a validator, and destroyed with the context.
- A factory of another type than the bean stops the startup with `BeanNotOfRequiredTypeException` of Spring; a
  factory of another annotation, with an `IllegalStateException`.
- A bean is replaced by a decorator of its interface: it is injected by the interface, not by its class.
- Spring looks for `@EventListener` and `@RequestMapping` on the class of the bean, which is now the decorator,
  and finds none; `@Scheduled` and listeners of messaging are bound before the decorators and call the bare bean.

`DecoratorFactoryBeanPostProcessor` wraps each bean of a type by the `DecoratorFactory` beans of the type, as
`WebServerFactoryCustomizerBeanPostProcessor` of Spring Boot applies each `WebServerFactoryCustomizer` bean to the
web server factories of its type:

```kotlin
@Configuration
@Import(DecoratedAnnotationBeanPostProcessor::class, DecoratorFactoryBeanPostProcessor::class)
class DecorationConfiguration {

    @Bean
    @Order(1)
    fun borderDecoratorFactory() = DecoratorFactory<VisualComponent> { target -> BorderDecorator(target, 1) }

    @Bean
    @Order(2)
    fun scrollDecoratorFactory() = DecoratorFactory<VisualComponent>(::ScrollDecorator)
}

@Component
class Caption : VisualComponent by TextView("caption")
```

The bean of `Caption`, as each bean of `VisualComponent`, is a `BorderDecorator` around a `ScrollDecorator` around
the caption itself:

- Factories apply in `@Order`: the first makes the outermost decorator, as advice of Spring AOP with the lowest
  `@Order` and `HttpHandlerDecoratorFactory` do. `CompositeTaskDecorator` of Spring and Scrutor make the first
  the innermost instead. The order of a factory comes from where `FactoryAwareOrderSourceProvider` of Spring takes
  it for a list of beans: the order of its bean definition, `@Order` on its `@Bean` method, `@Order` or `Ordered` on
  its class. A factory without one goes last.
- It replaces `@Primary` and `@Qualifier`, where each decorator injects the next by the name of its bean:
  a decorator knows neither its neighbours nor its place, a new one goes in between without a change of them,
  and the context has no undecorated component to inject by mistake.
- The type a factory decorates comes from its bean definition, the class of the factory or the return type of its
  `@Bean` method, so the post-processor makes no factory of another type: a `@Lazy` one stays unmade. A factory
  that hides its type, such as a lambda registered as a raw `DecoratorFactory`, stops the startup with an
  `IllegalStateException` that names it.
- Only singleton factories count, as only singleton customizers do for `WebServerFactoryCustomizerBeanPostProcessor`:
  a prototype or scoped `DecoratorFactory` is not seen.
- A factory is taken from the context when the first bean of its type is decorated: a factory that injects a bean
  of the type it decorates is a circular reference.
- The factories of a type wrap the decorators of annotations only when `DecoratorFactoryBeanPostProcessor` is
  registered after `DecoratedAnnotationBeanPostProcessor`, as in the `@Import` above: Spring applies such
  post-processors in the order of their registration, and the annotations of a bean are not seen through a
  decorator.
- As with annotations, the bean is injected by its interface and Spring finds no `@EventListener` on it. Nor does
  Spring start it as a `Lifecycle` or call it as an `ApplicationListener`, unless its decorators are ones too.

### Plan of Decorator

1. `Decorator` and `DecoratorFactory`.
2. `AnnotationDecoratorFactory`, `Decorated` and `DecoratedAnnotationBeanPostProcessor`.
3. `DecoratorFactoryBeanPostProcessor`.
4. An auto-configuration of Spring Boot that registers both post-processors.
5. Interceptors bound to annotations of methods over `AnnotationMatchingPointcut`, as `@Retryable` of Spring is,
   with a check at startup that a proxy reaches each annotated method: Spring only logs a final one.

## Proxy

Proxy, as the Gang of Four describe it, is a surrogate that controls the access to another object: it has
the interface of that object, its real subject, and passes a request to it on its own terms: later, by
rights, over the network or with a bookkeeping of its own, a count of references or a lock. The whole work
stays in the real subject. Take a proxy away and the work is the same, only the way to reach it changes;
take a decorator away and part of the work is gone. A proxy stands for one real subject and is not nested
as decorators are.

| Role | Who plays it | What it holds |
| --- | --- | --- |
| Subject | an interface of the application | the operations a client calls |
| RealSubject | a class that implements it | the whole work of the operations |
| Proxy | a class that implements `Proxy` and the subject | the delegate, private, and the control of each call to it |

`Proxy<T>` names the role, as `SpringProxy` marks a proxy of Spring AOP and `HibernateProxy` a proxy of
Hibernate. A proxy keeps the object the book calls the real subject private, as the book keeps `_image`: a
call that reached it past the proxy would go round the control. It shows `delegateClass`, the class of the
delegate, as `getTargetClass` of Spring `TargetClassAware` and `getPersistentClass` of a Hibernate
`LazyInitializer` do: an adapter finds the annotations of the class behind a proxy, as `AopUtils.getTargetClass`
does, and a log names it. The interface of the subject a proxy implements itself: a type parameter is no
supertype, so `Proxy<T>` cannot make a class a `T`. A client depends on the interface of the subject, not on
`Proxy`.

`isDelegateLoaded()` tells whether the delegate is there, without making it, as `isLoaded` of a JPA
`PersistenceUnitUtil`, `Hibernate.isInitialized` and `isInitialized` of a Kotlin `Lazy` do: a log or a serializer
skips a proxy whose delegate is not there instead of reading a file or a database for it, as
`HibernateProxySerializer` of the Jackson Hibernate module writes `null` for a proxy that is not initialized
unless `FORCE_LAZY_LOADING` is on. A virtual proxy answers by its `Lazy`; a protection proxy holds its delegate
from the start and answers `true`. The method has no default: a virtual proxy that forgot it would tell a lie.

A virtual proxy is given the class of its delegate, since it tells the class before it makes the delegate, as
Hibernate takes the class of an entity from its mapping and `LazyInitTargetSource` of Spring from the
definition of a bean. The delegate made later may be of a subclass.

A remote proxy has no `Proxy` type here: its delegate lives in another address space, and there is no
object here to load. Spring `HttpServiceProxyFactory`, Retrofit and Feign make such proxies of an
interface.

The example of the book, an image of a document loaded on its first draw:

```kotlin
interface Graphic {

    val extent: Int

    fun draw(): String
}

class ImageProxy(
    override val delegateClass: KClass<out Graphic>,
    initializeDelegate: () -> Graphic,
    override val extent: Int,
) : Graphic,
    Proxy<Graphic> {

    private val image = lazy(initializeDelegate)

    override fun isDelegateLoaded() = this.image.isInitialized()

    override fun draw() = this.image.value.draw()
}
```

The proxy answers `extent` from the document and reads the file only to draw the image, as `ImageProxy`
of the book does. The proxy of the book makes its image by the name of a file; this one takes the way to
load it, so a factory or a container decides where the image comes from. A protection proxy takes its delegate
ready, checks the rights on each call and forwards by `by` what it does not check. `by` takes a private
property or a plain parameter of the constructor as well:

```kotlin
class GuardedImage(private val image: Graphic, private val isAllowed: () -> Boolean) :
    Graphic by image,
    Proxy<Graphic> {

    override val delegateClass = this.image::class

    override fun isDelegateLoaded() = true

    override fun draw() = when {
        this.isAllowed() -> this.image.draw()
        else -> throw SecurityException("No rights to draw")
    }
}
```

`by` has the hole described for Decorator: a member that `Graphic` gains later goes to the delegate with no
check of rights. `by` keeps the object it was given when the proxy was made, so a virtual proxy cannot use it
without making the delegate at once: it writes each member and keeps the body a member has in the interface
instead of loading the image for it.

### Factories of proxies

A factory puts a delegate that comes from outside behind a proxy of its type. It takes `initializeDelegate`, a function
that makes the delegate, not the delegate itself: a virtual proxy of a ready object is late to save anything. A
virtual proxy keeps the function until the first call, a protection proxy calls it at once:

```kotlin
val virtual = ProxyFactory<Graphic> { image -> ImageProxy(Image::class, image, 50) }
val guarded = ProxyFactory<Graphic> { image -> image().let { graphic -> GuardedImage(graphic) { true } } }

val image = virtual.proxy { Image("photo.png") }
```

`proxy` is named as `proxy` of `AuthorizationProxyFactory` of Spring Security, which puts a ready object behind
a check of rights. `@Lazy` of Spring takes the way, as this factory does: the proxy it injects resolves the
bean by its `TargetSource` on the first call, as `LazyInitTargetSource` does. The compiler checks that a factory
returns the type of its delegate.

### Plan of Proxy

1. `Proxy` and `ProxyFactory`.
2. `AnnotationProxyFactory`, `Proxied` and a post-processor in `semantic-pattern-spring`, as for Decorator: an
   annotation of a bean puts it behind a proxy configured by the attributes of the annotation, as `@PreAuthorize`
   on a class of Spring Security does.
   - A bean stands behind one proxy: two proxy annotations on a class stop the startup with an error of the
     contract, since a proxy is not nested.
   - A virtual proxy is made before the bean is: `postProcessBeforeInstantiation` of an
     `InstantiationAwareBeanPostProcessor` returns it, as `LazyInitTargetSourceCreator` of Spring AOP makes a
     lazy proxy of a bean, and gives the factory a way to get the bean from the context, as `@Lazy` does. The
     class of the delegate comes from the definition of the bean.
   - A factory named by an annotation is a bean of its class, as for Decorator. A factory of another type
     than the bean stops the startup with an error of the contract.
   - A bean is replaced by a proxy of its interface: it is injected by the interface, not by its class.
3. A post-processor that puts each bean of a type behind the `ProxyFactory` bean of the type.

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

## Visitor

Visitor, as the Gang of Four describe it, adds an operation to a hierarchy without a change of its
classes: the operation is a class of its own, a visitor, with a visit method per class of element,
and an element calls the method of its class from `accept`. The library takes the fallback that the
visitors of `javax.lang.model`, IntelliJ PSI and Kotlin IR declare: the method each visit method
calls unless a visitor overrides it, and the `accept` of an element:

| Role | Who plays it | What it holds |
| --- | --- | --- |
| Visitor | an interface of the hierarchy that extends `Visitor` | a visit method per class of element, each falls back to `visit` |
| ConcreteVisitor | a class that implements it | one operation over every class of element |
| Element | the base type of the hierarchy, it implements `Visitable` | `accept`, abstract |
| ConcreteElement | a class of the hierarchy | `accept`, which checks the class of the visitor and calls the visit method of its class or `visit` |
| ObjectStructure | a `Composite` or a collection | the elements a visitor goes over |

The example of the book, equipment of a computer and the pricing of it:

```kotlin
abstract class Equipment :
    Composite<Equipment>,
    Visitable<Equipment> {

    abstract val price: Int
}

class FloppyDisk : Equipment() {

    override val price = 5

    override fun <R> accept(visitor: Visitor<Equipment, R>) = when (visitor) {
        is EquipmentVisitor<R> -> visitor.visitFloppyDisk(this)
        else -> visitor.visit(this)
    }
}

class Chassis(children: List<Equipment>) : Equipment() {

    override val children = children.toList()

    override val price = 20

    override fun <R> accept(visitor: Visitor<Equipment, R>) = when (visitor) {
        is EquipmentVisitor<R> -> visitor.visitChassis(this)
        else -> visitor.visit(this)
    }
}

interface EquipmentVisitor<out R> : Visitor<Equipment, R> {

    fun visitFloppyDisk(disk: FloppyDisk) = visit(disk)

    fun visitChassis(chassis: Chassis) = visit(chassis)
}

class PricingVisitor : EquipmentVisitor<Int> {

    override fun visitChassis(chassis: Chassis) = chassis.price + chassis.children.sumOf { child -> child.accept(this) }

    override fun visit(element: Equipment) = element.price
}

val computer = Chassis(listOf(Chassis(listOf(FloppyDisk())), FloppyDisk()))
val price = computer.accept(PricingVisitor())
```

`price` is 50. The visitor goes over the children here; the book lets `accept` of a container do it
as well, and Iterator walks a hierarchy for a visitor that needs no result of the children.

`visit` is the method of any element, and each visit method of a hierarchy falls back to it, as each
method that the JDK added to `ElementVisitor` in `javax.lang.model` after Java 6, where the
interface appeared, falls back to its `visitUnknown`: `visitModule` of Java 9 and
`visitRecordComponent` of Java 16 call it by default. So `visit` lowers the price of a new class of
element: the visitors written before it keep compiling, and a visitor overrides only the methods its
operation tells apart. A visitor that must not miss a class throws from the fallback, as
`visitUnknown` of `AbstractElementVisitor6` throws `UnknownElementException`. Unlike `visit` of
ANTLR and of `AbstractElementVisitor6`, this `visit` does not call `accept`: a visitor goes over a
child by `child.accept(this)`, not by `visit(child)`. A visitor still pays off where operations
change more often than classes, and a class of another module gets no visit method of its own. Where
a hierarchy is sealed, a `when` over its classes does the same, and the compiler checks it covers
each of them.

`Visitable` names an element as `Visitable` of Spring Data Relational, jpmml and Infinispan do.
Its `accept` is abstract, as `accept` of `Element` in `javax.lang.model` and of `Expression` in
Querydsl are, so a new class does not fall back to `visit` by mistake. It takes a visitor of the
base type: an `accept` that takes the visitor of each hierarchy and returns its result needs
higher-kinded types, which Kotlin does not have. So an element checks the class of the visitor, as
each `accept` that ANTLR generates does, and a visitor of the base type alone gets `visit`. The
check is one `is` per call, and the compiler needs no cast for it: the result type of
`EquipmentVisitor<R>` is the one of the visitor it checks.

A visitor keeps its context in its own properties or takes it in its constructor, as the visitors
of ANTLR do, whose methods take the node alone; the visit methods of `ElementVisitor<R, P>` and of
Querydsl `Visitor<R, C>` take it as a second parameter instead.

Composite plays ObjectStructure; Iterator walks it. Interpreter keeps the operation in the classes
of its expressions; a visitor takes it out of them and does the interpretation.

### Plan of Visitor

1. `Visitor`, the base type of the visitors of a hierarchy; `Visitable`, an element with `accept`.
2. A fold of a `Composite` by a result per element, `visitChildren`, `defaultResult`,
   `aggregateResult` and `shouldVisitNextChild`, as `AbstractParseTreeVisitor` of ANTLR folds:
   `visitChildren` calls `accept` of each child, which `Visitable` gives it.

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
