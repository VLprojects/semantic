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

<img src="docs/assets/families-light.svg#gh-light-mode-only" width="100%" alt="Families and their modules: ddd and cqrs in development, vo planned">
<img src="docs/assets/families-dark.svg#gh-dark-mode-only" width="100%" alt="Families and their modules: ddd and cqrs in development, vo planned">

| Family | What it is | Status |
| --- | --- | --- |
| `vo` | rich value objects: `semantic-vo` with the validation every value object shares, then `net`, `range`, `time` and more | planned for v1 |
| [`ddd`](ddd/README.md) | identifiers, entities, aggregate roots with events and invariants, business rules, domain errors; JPA, Spring Data and Spring Boot adapters | design, v1 |
| [`cqrs`](cqrs/README.md) | commands, queries, their handlers and buses; no dependency on `ddd` | design |

Artifacts are named `semantic-<family>[-<adapter>]` and live in the group
`pro.vlprojects.semantic`. Packages follow the same path:
`pro.vlprojects.semantic.ddd.jpa`.

Built for Java 25, Kotlin 2.4 and Spring Boot 4.1. Licensed under Apache 2.0.
