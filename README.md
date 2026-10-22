# semantic

Types that say what the code means. A family of small Kotlin libraries for the
domain layer of JVM services: the building blocks of domain-driven design, and
value objects for the things every domain has, such as an email, a range or a
quarter.

Each family has a core with no dependencies and adapters for the frameworks you
use. Take only what you need: a service that wants an `Email` does not get
aggregates with it.

| Family | What it is | Status |
| --- | --- | --- |
| [`ddd`](ddd/README.md) | identifiers, entities, aggregate roots with events and invariants, business rules, domain errors; JPA, Spring Data and Spring Boot adapters | design, v1 |
| `vo` | value objects: `semantic-vo` with the validation every value object shares, then `net`, `range`, `time` and more | planned for v1 |
| `cqrs` | commands, queries, handlers and buses | after v1 |

Artifacts are named `semantic-<family>[-<adapter>]` and live in the group
`pro.vlprojects.semantic`. Packages follow the same path:
`pro.vlprojects.semantic.ddd.jpa`.

Built for Java 25, Kotlin 2.4 and Spring Boot 4.1. Licensed under Apache 2.0.
