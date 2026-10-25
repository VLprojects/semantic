# semantic

A Swiss army knife for Kotlin: rich value objects, DDD building blocks and small
abstractions that make domain code say what it means.

Not a framework, but a set of small, independent tools. Each one fixes
something you would otherwise write again in every project: an `Email` that is
always valid, a range that knows its bounds, an aggregate root that does not
lose its events.

Each family has a core with no dependencies and adapters for the frameworks you
use. Take only what you need: a service that wants an `Email` does not get
aggregates with it.

<img src="docs/assets/families-light.svg#gh-light-mode-only" width="100%" alt="Families and their modules: helper and ddd in development, vo and cqrs planned">
<img src="docs/assets/families-dark.svg#gh-dark-mode-only" width="100%" alt="Families and their modules: helper and ddd in development, vo and cqrs planned">

| Family | What it is | Status |
| --- | --- | --- |
| [`helper`](helper/README.md) | extensions of the standard types, in groups: `text` so far | in development, v1 |
| `vo` | rich value objects: `semantic-vo` with the validation every value object shares, then `net`, `range`, `time` and more | planned for v1 |
| [`ddd`](ddd/README.md) | identifiers, entities, aggregate roots with events and invariants, business rules, domain errors; Spring Data adapters for JPA and MongoDB, Spring Boot | design, v1 |
| `cqrs` | commands, queries, handlers and buses | after v1 |

Artifacts are named `semantic-<family>[-<adapter>]` and live in the group
`pro.vlprojects.semantic`. Packages follow the same path:
`pro.vlprojects.semantic.ddd.spring.data.jpa`.

Built for Java 25, Kotlin 2.4 and Spring Boot 4.1. Licensed under Apache 2.0.
