# semantic-ddd

Building blocks of domain-driven design for Kotlin and Java: typed
identifiers, entities, aggregate roots, domain events, business rules,
specifications and domain errors, and the bounded contexts that hold them. The
core is plain Kotlin; Spring Data with its stores, JPA and
MongoDB, and Spring Boot live in modules of their own.

The contracts of the core are in place, and so is the base class of the
Spring Data module; the store and Spring Boot modules are empty so far.

## The core

Packages of `semantic-ddd`, under `pro.vlprojects.semantic.ddd`:

| Package | What is there |
| --- | --- |
| `tactical` | The building blocks, side by side: `DomainObject` they all extend, with `typeName`, the class name in kebab case, on an object and on its class; `Identifier`, `Identifiable`, `Entity`, `AggregateRoot` with the `Revision` it gives, `Versioned`, `ValueObject`, `Event` with its `EventMeta`, built by `EventMeta.of<ItemAdded>()` with a new `EventId`, the current time and the class name in kebab case by default, `BusinessRule`, `Specification` |
| `tactical.specification.composite` | The parts a specification is composed of: `AllOf`, `AnyOf` and `Not` hold other specifications, `Always` and `Never` hold for every candidate and for none, `allOf` and `anyOf` combine any number of specifications |
| `tactical.base` | Base classes to extend, free of any framework: `AbstractEntity` and `AbstractId` equal by identifier and value, `AbstractAggregateRoot` that registers events and gives a `BaseRevision` of itself, `AbstractEvent` equal by id, and `EventId` on a `UUID`, the default id of an event |
| `strategic` | The boundaries of the model: `BoundedContext`, declared by an object in the root package of the context and named after it in kebab case |
| `dsl` | The ways to write the blocks down: `rule` builds a `BusinessRule`, `check` runs a list of them, `checkRule` checks a one-off condition |
| `architecture.error` | What a layer of the application raises, not a pattern of the model: `DomainException` and the refusals a service extends, `NotFoundException`, `ConflictException`, `BusinessRuleException` |

A bounded context is an object, `object Billing : BoundedContext`, put in the
root package of the context: the package of the object and every package under
it belong to the context, so its classes need no mark of their own.
`Invoice::class in Billing` tells whether a class belongs to it. Being an
object, a context is referred to by its type, `Billing::class`, and the other
strategic patterns will name the contexts they relate in this way, so a typo
does not compile. The rest of them, subdomains, the relations of a context map
and the like, come one by one.

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
happened; the revision, `Revision`, taken by `aggregate.revision`, holds the
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

## Spring Data

`semantic-ddd-spring-data` gives `SpringDataAggregateRoot`, the base an
aggregate saved by a Spring Data repository extends, under
`pro.vlprojects.semantic.ddd.spring.data`. It is an `AbstractAggregateRoot` of
the core and a `Persistable`: `getId()` is the identifier, the registered events
are handed out with `@DomainEvents` when the repository saves the aggregate and
forgotten with `@AfterDomainEventPublication` once they are published. Events
are published by `save()` only: an aggregate changed and not saved publishes
nothing.

`isNew()` is left to the store, since each tells a new aggregate from a saved
one in its own way: the JPA base by the callbacks of the entity, the MongoDB base
by the version.

## Modules

![Modules, and which of them a service takes](../docs/assets/modules-light.svg#gh-light-mode-only)
![Modules, and which of them a service takes](../docs/assets/modules-dark.svg#gh-dark-mode-only)

Six artifacts, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-ddd` | any JVM code: the building blocks | `semantic-vo`, `semantic-format` |
| `semantic-ddd-spring-data` | what every Spring Data store shares: `Persistable`, events handed out on save | core, `spring-data-commons` |
| `semantic-ddd-spring-data-jpa` | aggregates mapped with JPA | the Spring Data module, `jakarta.persistence-api` |
| `semantic-ddd-spring-data-mongodb` | aggregates stored in MongoDB | the Spring Data module, `spring-data-mongodb` |
| `semantic-ddd-spring-boot` | auto-configuration | the Spring Data module, Spring Boot |
| `semantic-ddd-spring-boot-starter` | the usual way in: dependencies only | core, the Spring Data and Spring Boot modules |

A service takes the starter and the module of its store, JPA or MongoDB: the
starter does not pick a store for it. JPA without Spring Data is not supported.

The naming follows the Spring Boot reference: a module of your own is
`<name>-spring-boot`, the starter is `<name>-spring-boot-starter`. A store
module is named as in Spring Data, `spring-data-jpa` and `spring-data-mongodb`.
Configuration keys live under `semantic.ddd`.

Directories drop the prefix the path already gives:

```
ddd/
├── core/                  semantic-ddd
├── spring-data/           semantic-ddd-spring-data
├── spring-data-jpa/       semantic-ddd-spring-data-jpa
├── spring-data-mongodb/   semantic-ddd-spring-data-mongodb
├── spring-boot/           semantic-ddd-spring-boot
└── spring-boot-starter/   semantic-ddd-spring-boot-starter
```

Packages follow the artifacts: `pro.vlprojects.semantic.ddd`,
`pro.vlprojects.semantic.ddd.spring.data`,
`pro.vlprojects.semantic.ddd.spring.data.jpa`,
`pro.vlprojects.semantic.ddd.spring.data.mongodb`,
`pro.vlprojects.semantic.ddd.spring.boot`. A package belongs to one artifact
only. The base classes of a store carry its name, `JpaAggregateRoot` and
`MongoAggregateRoot`, so they do not clash with those of the core.

---

The diagram is generated by `docs/diagrams/build.py`, in a light and a dark
variant. It needs nothing but Python.
