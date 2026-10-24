# semantic-ddd

Tactical building blocks of domain-driven design for Kotlin and Java: typed
identifiers, entities, aggregate roots, domain events, business rules and domain
errors. The core is plain Kotlin; Spring Data with its stores, JPA and
MongoDB, and Spring Boot live in modules of their own.

The contracts of the core are in place, so are the base class of the
Spring Data module and the publisher of events in Spring Boot; the store
modules are empty so far.

## The core

Packages of `semantic-ddd`, under `pro.vlprojects.semantic.ddd`:

| Package | What is there |
| --- | --- |
| `tactical` | The building blocks, side by side: `DomainObject` they all extend, with `typeName`, the class name in kebab case, on an object and on its class; `Identifier`, `Identifiable`, `Entity`, `AggregateRoot` with the `Revision` it gives, `Versioned`, `ValueObject`, `Event` with its `EventMeta`, built by `EventMeta.of<ItemAdded>()` with a new `EventId`, the current time and the class name in kebab case by default, `BusinessRule` |
| `tactical.base` | Base classes to extend, free of any framework: `AbstractEntity` and `AbstractId` equal by identifier and value, `AbstractAggregateRoot` that registers events, hands them out once with `pullEvents()` and gives a `BaseRevision` of itself, `AbstractEvent` equal by id, and `EventId` on a `UUID`, the default id of an event |
| `dsl` | The ways to write the blocks down: `rule` builds a `BusinessRule`, `check` runs a list of them, `checkRule` checks a one-off condition |
| `architecture.error` | What a layer of the application raises, not a pattern of the model: `DomainException` and the refusals a service extends, `NotFoundException`, `ConflictException`, `BusinessRuleException` |
| `architecture.event` | `EventPublisher`, the port the application hands pulled events to |

The strategic patterns, bounded contexts and the like, will get a `strategic`
package of their own.

The contracts are interfaces; `tactical.base` gives classes to extend. What
needs a framework, handing the events out on save, telling a new aggregate from
a saved one, the mapping of the identifier and the version and the proxies, is
left to the Spring Data modules.

The property of `Identifiable` is `identifier`, not `id`: an aggregate saved by
Spring Data also implements `Persistable`, and its `getId()` would clash with a
Kotlin `id` (KT-6653).

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

Without Spring, the application saves the aggregate and hands what
`pullEvents()` returns to an `EventPublisher` of its own, in the same unit of
work: `cart.pullEvents().forEach(publisher::publish)`. `pullEvents()` clears the
events, so they are handed out once. The domain itself publishes nothing.
`Entity` and `AggregateRoot` take themselves as the first type parameter,
`AggregateRoot<Cart, CartId>`, so that a contract can speak of the concrete
type.

## Spring Data

`semantic-ddd-spring-data` gives `SpringDataAggregateRoot`, the base an
aggregate saved by a Spring Data repository extends, under
`pro.vlprojects.semantic.ddd.spring.data`. It is an `AbstractAggregateRoot` of
the core and a `Persistable`: `getId()` is the identifier, the registered events
are handed out with `@DomainEvents` when the repository saves the aggregate and
forgotten with `@AfterDomainEventPublication` once they are published. Events
are published by `save()` only: an aggregate changed and not saved publishes
nothing, unless the application pulls its events and hands them to the
`EventPublisher` itself.

`isNew()` is left to the store, since each tells a new aggregate from a saved
one in its own way: the JPA base by the callbacks of the entity, the MongoDB base
by the version.

## Spring Boot

`semantic-ddd-spring-boot` registers an `EventPublisher` on the
`ApplicationEventPublisher` of the context, unless the service declares one of
its own. Events handed to it and events Spring Data publishes on `save()` take
one path: the same `@EventListener`, `@TransactionalEventListener` and Spring
Modulith listeners receive them, and the Modulith event publication registry, the
outbox, records both. The delivery, in the transaction, after the commit or
through the outbox to a broker, is chosen by the listener, not by the publisher.

## Modules

![Modules, and which of them a service takes](../docs/assets/modules-light.svg#gh-light-mode-only)
![Modules, and which of them a service takes](../docs/assets/modules-dark.svg#gh-dark-mode-only)

Six artifacts, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-ddd` | any JVM code: the building blocks | `semantic-vo` |
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
