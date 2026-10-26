# semantic-ddd

Tactical building blocks of domain-driven design for Kotlin and Java: typed
identifiers, entities, aggregate roots, domain events, business rules,
specifications and domain errors. The core is plain Kotlin; Jackson, Spring Data with its stores,
JPA and MongoDB, and Spring Boot live in modules of their own.

The contracts of the core are in place, and so are the base classes of the
Spring Data and JPA modules and the whole Jackson module; the MongoDB and Spring
Boot modules are empty so far.

## The core

Packages of `semantic-ddd`, under `pro.vlprojects.semantic.ddd`:

| Package | What is there |
| --- | --- |
| `tactical` | The building blocks, side by side: `DomainObject` they all extend, with `typeName`, the class name in kebab case, on an object and on its class; `Identifier`, `BoxedIdentifier`, a key that boxes one serializable value such as a `UUID` or a `Long`, `Identifiable`, `Entity` with its version, `AggregateRoot`, `Revision` of an aggregate, `Versioned`, `ValueObject`, `Event` with its `EventMeta`, built by `EventMeta.of<ItemAdded>()` with a new `EventId`, the current time and the class name in kebab case by default, `BusinessRule`, `Specification` |
| `tactical.specification.composite` | The parts a specification is composed of: `AllOf`, `AnyOf` and `Not` hold other specifications, `Always` and `Never` hold for every candidate and for none, `allOf` and `anyOf` combine any number of specifications |
| `tactical.base` | Base classes to extend, free of any framework: `AbstractEntity` and `AbstractId` equal by identifier and value, the version of an entity set by `assignVersion`, `AbstractAggregateRoot` that registers events, `BaseRevision` built of any aggregate, `AbstractEvent` equal by id, and `EventId` on a `UUID`, the default id of an event |
| `dsl` | The ways to write the blocks down: `rule` builds a `BusinessRule`, `check` runs a list of them, `checkRule` checks a one-off condition |
| `architecture.error` | What a layer of the application raises, not a pattern of the model: `DomainException` and the refusals a service extends, `NotFoundException`, `ConflictException`, `BusinessRuleException` |

The strategic patterns, bounded contexts and the like, will get a `strategic`
package of their own.

The contracts are interfaces; `tactical.base` gives classes to extend. What
needs a framework, handing the events out on save, telling a new aggregate from
a saved one, the mapping of the identifier and the version and the proxies, is
left to the Spring Data modules.

The property of `Identifiable` is `identifier`, not `id`: an aggregate saved by
Spring Data also implements `Persistable`, and its `getId()` would clash with a
Kotlin `id` (KT-6653).

An `Identifier` is a `ValueObject`, and the only block that is `Serializable`:
JPA asks it of the class of a key, `@EmbeddedId` and `@IdClass`, and a
distributed cache or a session stored outside the process serializes the key.
Entities, value objects and events are not: nothing in the core sends them
through Java serialization, and a service that needs it marks its own class.

An event carries its meta, the revision of its aggregate and fields of its own. The
meta, `EventMeta`, holds the id of the event, its name and the time it
happened; the revision, `Revision`, built by `BaseRevision.of(aggregate)`, holds the
name, the identifier and the version of the aggregate. Both are set once when the event
is raised. The name of an event and of an aggregate is its `typeName`, the class in kebab case, `ItemAdded` is
`item-added` and `ShoppingCart` is `shopping-cart`, so renaming a class
renames it for the receivers. An event is typed by its aggregate and the identifier of
it, `Event<Cart, CartId>`, so an event cannot name an aggregate of another
type. How an aggregate records its events and hands them out is left to it and
to the adapter that saves it, as Spring Data does with `@DomainEvents`.
`Entity` and `AggregateRoot` take themselves as the first type parameter,
`AggregateRoot<Cart, CartId>`, so that a contract can speak of the concrete
type.

## Specifications

A specification is a rule of the domain that a candidate satisfies or not:
an aggregate, an entity, a value object, a collection. It is a
`(T) -> Boolean`, so it goes wherever a predicate does, `orders.filter(IsOpen)`,
and `rule(IsOpen) { "Closed" }` makes a business rule of it. A specification is
a domain object, not a value object: it holds a rule, not data of the model.

Specifications compose into a tree. `AllOf` holds when every part holds,
`AnyOf` when one does, `Not` when its part does not. A node is equal to another
by its parts, in any order, so a test can compare the rule a service built.
`Always` and `Never` are the neutral elements, the rule with no condition and
the rule nothing satisfies.

`and`, `or` and `not` build the tree: `IsOpen and !Paid` is an `AllOf` of
`IsOpen` and a `Not` of `Paid`. The tree comes out simplified: a nested `AllOf`
is flattened, `Always` drops out of a conjunction and `Never` absorbs it, `!!a`
is `a`. Like any infix call in Kotlin, `and` and `or` bind equally, left to
right: `a or b and c` is `(a or b) and c`, so a mix of them needs parentheses.

`allOf(a, b, c)` and `anyOf(a, b, c)`, or the same on a list, combine any
number of them. They skip a null, so a rule built of optional filters takes
the ones that are set, `allOf(IsOpen, sku?.let(::ContainsSku))`. With nothing
to combine `allOf` is `Always` and `anyOf` is `Never`. A null that should not
be there weakens the rule silently: a filter that must hold is passed as it is,
never through `?.let`.

## Jackson

`semantic-ddd-jackson` gives `SemanticDddModule`, a module of Jackson 3 under
`pro.vlprojects.semantic.ddd.jackson`. It writes a `BoxedIdentifier` as the
value it boxes, `42` or `"0192f0c1-…"`, not `{"value": 42}`, and reads it back
through the constructor of the identifier that takes that value. It adds no
serializer or deserializer of its own: Jackson treats `value` as if it carried
`@JsonValue` and that constructor as if it carried
`@JsonCreator(mode = DELEGATING)`, so the identifier needs no annotation. A
value the constructor rejects fails the reading, as any other invalid input
does.

```kotlin
val mapper = JsonMapper.builder()
    .addModule(SemanticDddModule())
    .build()
```

The module is listed for `ServiceLoader`, so `findAndAddModules()` finds it,
and so does Spring Boot, which looks for modules by default.

The module reads an identifier only through its public constructor of the value
and turns off every other constructor that takes an argument, a private one or
one of another type, with jackson-module-kotlin too. An identifier built by a
factory hides its constructor and marks that factory with `@JsonCreator`, in
Kotlin with `@JvmStatic` in the companion; a `@JsonCreator` or
`@ConstructorProperties` on a constructor wins over the module. An identifier
written another way turns `value` off with `@get:JsonValue(false)` and marks its
own member with `@get:JsonValue`.

A key of a map is written as the value it boxes. Jackson by itself reads a key
back only through a constructor or a factory of a `String`, and the module turns
off every constructor but the one of the value: an identifier of a `String` is
read back from a key, an identifier of another type through a factory of a
`String` marked `@JsonCreator` or named `valueOf` or `fromString`, or through a
key deserializer of its own.

## Spring Data

`semantic-ddd-spring-data` gives `AbstractSpringDataAggregateRoot`, the base an
aggregate saved by a Spring Data repository extends, under
`pro.vlprojects.semantic.ddd.spring.data`. It is an `AggregateRoot` and a
`Persistable`: `getId()` is the identifier, the registered events
are handed out with `@DomainEvents` when the repository saves the aggregate and
forgotten with `@AfterDomainEventPublication` once they are published. Events
are published by `save()` only: an aggregate changed and not saved publishes
nothing.

`AbstractSpringDataEntity` is the base of an entity: it extends
`AbstractEntity` of the core, with its identifier, version and invariants, and
is a `Persistable` whose `getId()` is the identifier.

Each module has a hierarchy of its own, the root extending the entity of the
same module: `AbstractSpringDataAggregateRoot` extends
`AbstractSpringDataEntity`, not `AbstractAggregateRoot`, and repeats the
events of the core root, since a class has one superclass.
`identifier` and `version` of the core are open, so the base of a store
overrides them with its mapping.

`isNew()` is true until `markPersisted()` is called. Neither this flag nor the
events of a root are stored: the `transient` modifier keeps them out of JPA,
`@Transient` of Spring Data out of the other stores. Marking an object persisted on load and after save is left
to the store modules: JPA through the callbacks of the entity, MongoDB through
the callbacks of its mapping.

Equality compares the classes through `ProxyUtils.getUserClass`, so the
proxy class of a store counts as the class of the entity.

## JPA

`semantic-ddd-spring-data-jpa` gives `AbstractJpaEntity` and
`AbstractJpaAggregateRoot`, under `pro.vlprojects.semantic.ddd.spring.data.jpa`.
Both are mapped superclasses: an entity of the service extends one of them and
is an `@Entity`. Each module has a hierarchy of its own, the root extending
the entity of the same module: `AbstractJpaEntity` extends
`AbstractSpringDataEntity`, `AbstractJpaAggregateRoot` extends
`AbstractJpaEntity` and repeats the events of the Spring Data
root, since a class has one superclass.

```kotlin
@Embeddable
class CartId(override val value: Long) : BoxedIdentifier<Long>

@Entity
class Cart(identifier: CartId) : AbstractJpaAggregateRoot<Cart, CartId>() {

    init {
        this.identifier = identifier
    }
}
```

The Spring Data bases know nothing of JPA, so the JPA bases override their
members with the mapping: `identifier` is the `@EmbeddedId`, its `value`
stored in the column `id`, neither null nor updated; `version` is the
`@Version`; `markPersisted()` runs on `@PostPersist` and `@PostLoad`, so
`isNew()` turns false once the entity is persisted or loaded. The key of a
JPA base is a `BoxedIdentifier`, so the value it boxes is always there to map; a
composite key needs a base of its own.

The class of the key does not extend `AbstractId` of the core: JPA builds it by
a constructor with no arguments, and `AbstractId` has none. It implements
`BoxedIdentifier` and its own equality.

`identifier` and `version` are open, so a lazy proxy of Hibernate answers
with the key it holds, not with its own empty field, and an entity equals its
proxy both ways: `ProxyUtils` takes the class of the entity from the proxy by
the detector of Spring Data JPA.

## Modules

![Modules, and what each takes from the one it builds on](../docs/assets/dependencies-light.svg#gh-light-mode-only)
![Modules, and what each takes from the one it builds on](../docs/assets/dependencies-dark.svg#gh-dark-mode-only)

Eight artifacts, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-ddd` | any JVM code: the building blocks | `semantic-format` |
| `semantic-ddd-jackson` | identifiers in JSON, with Spring or without | core, `jackson-databind` 3 |
| `semantic-ddd-spring-data` | what every Spring Data store shares: `Persistable`, events handed out on save | core, `spring-data-commons`, `spring-aop` |
| `semantic-ddd-spring-data-jpa` | aggregates mapped with JPA | the Spring Data module, `jakarta.persistence-api`, `spring-data-jpa` |
| `semantic-ddd-spring-data-mongodb` | aggregates stored in MongoDB | the Spring Data module, `spring-data-mongodb` |
| `semantic-ddd-spring-boot` | auto-configuration, empty so far: [what it may hold](spring-boot/autoconfigure/README.md) | Spring Boot; the modules it configures are `compileOnly` |
| `semantic-ddd-spring-boot-starter-jpa` | the way in for a JPA service: dependencies only | the Spring Boot and JPA modules, `spring-boot-starter-data-jpa` |
| `semantic-ddd-spring-boot-starter-mongodb` | the way in for a MongoDB service: dependencies only | the Spring Boot and MongoDB modules, `spring-boot-starter-data-mongodb` |

A service takes the starter of its store, JPA or MongoDB, and gets with it the
modules of the family for that store and the Spring Boot starter of that store;
`semantic-ddd-jackson` the service adds itself. JPA without Spring Data is not
supported.

The naming follows the Spring Boot reference: a module of your own is
`<name>-spring-boot`, a starter is `<name>-spring-boot-starter-<store>`. A store
module is named as in Spring Data, `spring-data-jpa` and `spring-data-mongodb`.
Configuration keys live under `semantic.ddd`.

Directories follow the types of module, core, Jackson, Spring and Spring Boot,
and drop the prefix the path already gives:

```
ddd/
├── core/                    semantic-ddd
├── jackson/                 semantic-ddd-jackson
├── spring/
│   └── data/
│       ├── core/            semantic-ddd-spring-data
│       ├── jpa/             semantic-ddd-spring-data-jpa
│       └── mongodb/         semantic-ddd-spring-data-mongodb
└── spring-boot/
    ├── autoconfigure/       semantic-ddd-spring-boot
    └── starter/
        ├── jpa/             semantic-ddd-spring-boot-starter-jpa
        └── mongodb/         semantic-ddd-spring-boot-starter-mongodb
```

A module lies only at the bottom of the tree; a directory above it only groups.
A new store goes next to its kin: its Spring Data module under `spring/data/`,
its starter under `spring-boot/starter/`.

Packages follow the artifacts: `pro.vlprojects.semantic.ddd`,
`pro.vlprojects.semantic.ddd.jackson`,
`pro.vlprojects.semantic.ddd.spring.data`,
`pro.vlprojects.semantic.ddd.spring.data.jpa`,
`pro.vlprojects.semantic.ddd.spring.data.mongodb`,
`pro.vlprojects.semantic.ddd.spring.boot`. A package belongs to one artifact
only. The base classes of a store carry its name, `AbstractJpaAggregateRoot` and
`AbstractMongoAggregateRoot`, so they do not clash with those of the core.

---

The diagram is generated by `docs/diagrams/build.py`, in a light and a dark
variant. It needs nothing but Python.
