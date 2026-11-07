# Semantic

Building blocks for Kotlin services, from the standard library up to the
architecture of a service: idioms, value objects, business rules and failures,
domain-driven design, architecture styles and patterns.

Not a framework, but a set of small, independent modules. Each one names
something every project writes again: a version that knows its order, a rule
that says why it failed, an aggregate root that does not lose its events, an
outbox that sends a message only after the transaction commits.

Each module has a core with no dependencies and adapters for the frameworks
you use. Take only what you need: a service that wants a feature flag does not
get aggregates with it.

<img src="docs/assets/families-light.svg#gh-light-mode-only" width="100%" alt="The tree of the library: architecture, correctness, design, idiom, math and type, their modules and the packages of each">
<img src="docs/assets/families-dark.svg#gh-dark-mode-only" width="100%" alt="The tree of the library: architecture, correctness, design, idiom, math and type, their modules and the packages of each">

## Structure

Six roots, in alphabetical order. A heading is a directory; a directory that is
divided further has a table of its own under it.

| Root | What it holds | Contents |
| --- | --- | --- |
| [`architecture`](#architecture) | styles of a service and the patterns it applies | boundary, pattern, style |
| [`correctness`](#correctness) | the rules a state keeps and the failures that report a broken one | constraint, error, rule, validation |
| [`design`](#design) | approaches to designing a model and its abstractions | algebra, dbc, ddd |
| [`idiom`](#idiom) | extensions of the Kotlin standard library | format, helper |
| [`math`](#math) | computations over numbers | math |
| [`type`](#type) | rich value objects, a module per category | version |

### Architecture

`architecture/` — how a service is put together: the style that decides which layer may call which, the boundary it shows to other systems, the patterns that keep it working under load and failure.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`boundary`](architecture/README.md) | a class marked as part of a contract with other systems, its scope and version | `boundary` | design |
| [`pattern`](#pattern) | a module per pattern or per group of patterns | caching, coordination, delivery, lifecycle, persistence, resilience, rollout, traffic | |
| [`style`](#style) | styles of a service, each extending the one before: layered, onion, clean, explicit | clean, explicit, layered, onion | |

#### Pattern

`architecture/pattern/` — answers to problems every service meets: delivering a message exactly once, outliving a failing dependency, holding back excess load, stopping without losing work, releasing a feature apart from a deployment.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`caching`](architecture/pattern/caching/core) | cache-aside, read-through, write-through, write-behind, refresh-ahead | | planned |
| [`coordination`](architecture/pattern/coordination/core) | sagas with compensating steps; a distributed lock with a lease and a fencing token | `saga`, `lock` | planned |
| [`delivery`](architecture/pattern/delivery/core) | transactional outbox (at-least-once sending) and inbox (at-most-once processing), together exactly-once; idempotency keys | `outbox`, `inbox`, `idempotency` | planned |
| [`lifecycle`](architecture/pattern/lifecycle/core) | graceful startup and shutdown, warm-up, liveness and readiness checks | `startup`, `warmup`, `health`, `shutdown` | planned |
| [`persistence`](#persistence) | how a service writes and reads its state | cqrs, event-sourcing, unit-of-work | |
| [`resilience`](architecture/pattern/resilience/core) | circuit breaker, retries with backoff and jitter, timeout, bulkhead, fallback | `circuitbreaker`, `retry`, `backoff`, `timeout`, `bulkhead`, `fallback` | planned |
| [`rollout`](architecture/pattern/rollout/core) | feature flags, kill switch, canary release, dark launch, A/B experiments | `featureflag`, `killswitch`, `canary`, `darklaunch`, `experiment` | planned |
| [`traffic`](architecture/pattern/traffic/core) | rate limiting, throttling, load shedding, back pressure | `ratelimit`, `throttling`, `shedding`, `backpressure` | planned |

##### Persistence

`architecture/pattern/persistence/` — how a service writes and reads its state: a model for each side, a history of events instead of the last state, many changes in one commit.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`cqrs`](architecture/pattern/persistence/cqrs/README.md) | commands and queries apart, their handlers and buses | `command`, `query`, `bus` | design |
| [`event-sourcing`](architecture/pattern/persistence/event-sourcing/core) | state rebuilt from its events, snapshots, projections | `store`, `snapshot`, `projection` | planned |
| [`unit-of-work`](architecture/pattern/persistence/unit-of-work/core) | tracked changes committed in one transaction | `tracking`, `commit` | planned |

#### Style

`architecture/style/` — styles from the simplest to the strictest, each extending the one before. A style names the layers and which may depend on which.

Each style holds `layer`, the annotations of layers, and `error`, the exceptions
of each layer.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`clean`](architecture/style/clean/core) | entities, use cases, adapters, frameworks; dependencies point inward | `layer`, `error` | planned |
| [`explicit`](architecture/README.md) | onion and clean combined, without ports and adapters | `layer`, `error` | design |
| [`layered`](architecture/style/layered/core) | presentation, application, domain, infrastructure tiers, each calling only the one below | `layer`, `error` | planned |
| [`onion`](architecture/style/onion/core) | the domain in the centre, rings around it | `layer`, `error` | planned |

### Correctness

`correctness/` — what a correct state is and what happens when it breaks: rules and constraints say what must hold, validation checks them all at once, an error reports what failed and where.

Depends on nothing: `ddd` and the architecture styles build on it.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`constraint`](correctness/constraint/core) | constraints of a value declared once: bounds, length, pattern, checked by validation | | planned |
| [`error`](correctness/error/core) | the model of a failure: a typed result, raising and recovering, failures accumulated | `failure`, `result`, `raise`, `accumulate` | planned |
| [`rule`](correctness/rule/core) | business rules: a named check that answers with a failure when it does not hold | `dsl`, `composite` | planned |
| [`validation`](correctness/validation/core) | every rule of a value checked at once, each failure with the path it came from | `validator`, `path` | planned |

### Design

`design/` — ways of designing a model: domain-driven design for the business, design by contract for the guarantees of a call, algebra for the laws of operations.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`algebra`](design/algebra/core) | orders and algebraic structures with their laws, the vocabulary of Algebra-Driven Design | `relation`, `structure` | planned |
| [`dbc`](design/dbc/core) | design by contract: conditions of a call and invariants of a type, checked at run time | `condition`, `invariant` | planned |
| [`ddd`](#ddd) | domain-driven design with Jackson, Spring Data and Spring Boot adapters | core, jackson, spring-boot, spring/data | |

#### DDD

`design/ddd/` — tactical domain-driven design. The core knows no framework; the adapters store aggregates with Spring Data and wire them with Spring Boot, so a service takes one starter and gets the rest.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`core`](design/ddd/README.md) | tactical DDD without a framework: identifiers, entities, aggregate roots with events, specifications, domain errors | `tactical`, `dsl`, `architecture.error`, `support` | design, v1 |
| [`jackson`](design/ddd/jackson) | an identifier in JSON as the value it boxes; no Spring | | design, v1 |
| [`spring-boot`](#spring-boot) | auto-configuration of the modules and the starters of a service | autoconfigure, starter | |
| [`spring/data`](#spring-data) | bases of aggregates a Spring Data repository saves, and their mapping | core, jpa, mongodb | |

##### Spring Boot

`design/ddd/spring-boot/` — a Spring Boot service wired without code: auto-configuration registers beans for the modules it finds, a starter brings those modules in one dependency.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`autoconfigure`](design/ddd/spring-boot/autoconfigure/README.md) | beans of the modules on the classpath, under `semantic.ddd` | `events`, `repository`, `mongodb`, `verification`, `web` | design, v1 |
| [`starter`](#starter) | the dependencies of a service, no code | jpa, mongodb | |

###### Starter

`design/ddd/spring-boot/starter/` — one dependency per store, with no code: the modules a service on that store needs.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`jpa`](design/ddd/spring-boot/starter/jpa) | the dependencies of a JPA service | | design, v1 |
| [`mongodb`](design/ddd/spring-boot/starter/mongodb) | the dependencies of a MongoDB service | | design, v1 |

##### Spring Data

`design/ddd/spring/data/` — bases of aggregate roots and entities a Spring Data repository saves, their events published on save; a module per store maps them.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`core`](design/ddd/spring/data/core) | bases of aggregates a Spring Data repository saves, events published on save | `data` | design, v1 |
| [`jpa`](design/ddd/spring/data/jpa) | JPA mapping of the bases through `orm.xml` | `data.jpa` | design, v1 |
| [`mongodb`](design/ddd/spring/data/mongodb) | MongoDB mapping of the bases | `data.mongodb` | design |

### Idiom

`idiom/` — what the Kotlin standard library lacks: extensions that read as part of the language, and values written out for a reader.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`format`](idiom/format/README.md) | a value written for a reader: letter cases, plurals, abbreviation, masking of a sensitive value | `abbreviation`, `mask` | in development, v1 |
| [`helper`](idiom/helper/README.md) | extensions of the standard types, grouped by the type they extend: Unicode-aware text, number signs and safe narrowing, `Duration` of a number | `text`, `number`, `time` | in development, v1 |

### Math

`math/` — computations business code repeats: rounding with the right mode and scale, statistics of a collection.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`core`](math/core) | rounding modes and money scale; mean, median, percentiles of a collection | `rounding`, `statistics` | planned |

### Type

`type/` — rich value objects: a value that checks itself when created and carries its own operations, so a model speaks in versions, not strings. A module per category.

| Directory | What it is | Contents | Status |
| --- | --- | --- | --- |
| [`version`](type/version) | a version as a value: order, stages, compatibility, increment; sequential, semantic and calendar schemes | | design |

Artifacts are named `semantic-<module>[-<adapter>]` and live in the group
`pro.vlprojects.semantic`. Packages follow the same path:
`pro.vlprojects.semantic.ddd.spring.data.jpa`.

Directories follow the structure above: a category is only a directory, a
module lies only at a leaf. The core of a module is `<path>/core`, a Spring
Data store of ddd is `design/ddd/spring/data/<store>`.

Built for Java 25, Kotlin 2.4 and Spring Boot 4.1. Licensed under Apache 2.0.
