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

<img src="docs/assets/families-light.svg#gh-light-mode-only" width="100%" alt="The tree of the library: idiom, type, math, correctness, design and architecture, their modules and the packages of each">
<img src="docs/assets/families-dark.svg#gh-dark-mode-only" width="100%" alt="The tree of the library: idiom, type, math, correctness, design and architecture, their modules and the packages of each">

## Structure

Six roots, from the language up to the architecture of a service. A module is
named by its artifact and lies in the directory after it; under it are its
packages and what each one holds.

### idiom

Extensions of the Kotlin standard library.

- **`semantic-helper`** `idiom/helper/core`: extensions of the Kotlin and JDK
  standard types, grouped by the type they extend.
  - `text`: Unicode-aware work with characters and strings: user-perceived
    symbols (grapheme clusters), words, checks of a prefix and a suffix.
    - `predicate`: checks of a character or a string: `isHyphen`,
      `isWildcard`, `isCapital`, `isAscii`, `isWord`.
    - `sanitizing`: cleaning of input: collapsing repeated spaces, dots,
      hyphens and apostrophes, removing diacritics, keeping only ASCII.
    - `transforming`: reshaping of a string: splitting into words, taking the
      first or the last symbols, normalizing spaces.
  - `number`: sign checks (`isPositive`, `isZero`) and narrowing that fails
    instead of overflowing (`toSafeInt`, `toSafeShort`).
  - `time`: a `java.time` `Duration` or `Period` built from a number:
    `5.minutes`, `2.weeks`, `1.quarter`.
- **`semantic-format`** `idiom/format/core`: a value written for a reader:
  letter cases (camel, snake, kebab) and plural forms of a counted word.
  - `abbreviation`: text shortened at its start, middle or end, with an
    ellipsis.
  - `mask`: characters of a sensitive value hidden at its start, middle or
    end: a card number, a phone, an email.

### type

Rich value objects, a module per category.

- **`semantic-type-version`** `type/version`: a version as a value: its order,
  stages, compatibility and increment, parsed from text; sequential (`3`),
  semantic (`2.1.0-rc.1`) and calendar (`2026.10`) schemes.

### math

- **`semantic-math`** `math/core`: computations over numbers.
  - `rounding`: rounding modes and scale, money scale included.
  - `statistics`: mean, median, variance and percentiles of a collection.

### correctness

What is right and what went wrong: the rules a state must keep and the
failures that report a broken one. Depends on nothing: neither on `ddd` nor on
an architecture style; they build on it.

- **`semantic-rule`** `correctness/rule/core`: business rules: a named check
  that answers with a failure when it does not hold.
  - `dsl`: the rule DSL: `rule`, `checkRule`.
  - `composite`: rules combined: all of, any of, not.
- **`semantic-validation`** `correctness/validation/core`: validation of a
  value: every rule it must keep checked at once, every failure reported with
  the place it came from.
  - `validator`: validators of a type, built from rules.
  - `path`: the path to the field or the element that failed.
- **`semantic-error`** `correctness/error/core`: the model of a failure.
  - `failure`: the base failure type: a code, a message, a cause.
  - `result`: a typed result, success or failure, chained in the railway
    style.
  - `raise`: raising a failure and recovering from it.
  - `accumulate`: all failures of a check collected at once, instead of
    stopping at the first.

### design

Approaches to designing a model and its abstractions.

`ddd`, domain-driven design:

- **`semantic-ddd`** `design/ddd/core`: tactical DDD without a framework.
  - `tactical`: identifiers, entities, value objects, aggregate roots with
    their events and versions, business rules, specifications.
    - `base`: abstract classes to extend: `AbstractId`, `AbstractEntity`,
      `AbstractAggregateRoot`, `AbstractEvent`.
    - `specification.composite`: specifications combined: `AllOf`, `AnyOf`,
      `Not`, `Always`, `Never`.
  - `dsl`: the business rule DSL: `rule`, `checkRule`.
  - `architecture.error`: `DomainException` and its kinds.
  - `support`: collection helpers for aggregates.
- **`semantic-ddd-jackson`** `design/ddd/jackson`: a Jackson module that writes
  an identifier in JSON as the value it boxes and reads it back; no Spring.
- **`semantic-ddd-spring-data`** `design/ddd/spring/data/core`: bases of
  aggregate roots and entities that a Spring Data repository saves, their
  events published on save.
- **`semantic-ddd-spring-data-jpa`** `design/ddd/spring/data/jpa`: JPA mapping
  of the bases through `orm.xml`.
- **`semantic-ddd-spring-data-mongodb`** `design/ddd/spring/data/mongodb`:
  MongoDB mapping of the bases.
- **`semantic-ddd-spring-boot`** `design/ddd/spring-boot/autoconfigure`: beans
  for the modules found on the classpath, configured under `semantic.ddd`.
  - `events`: domain events published after the transaction commits.
  - `jackson`: identifiers and value objects written as plain JSON values.
  - `mongodb`: MongoDB converters of identifiers and value objects.
  - `verification`: checks at startup that aggregates and entities are mapped
    right.
  - `web`: domain errors answered as HTTP problem details (RFC 9457).
- **`semantic-ddd-spring-boot-starter-jpa`**
  `design/ddd/spring-boot/starter/jpa` and
  **`semantic-ddd-spring-boot-starter-mongodb`**
  `design/ddd/spring-boot/starter/mongodb`: the dependencies of a JPA or a
  MongoDB service, no code.

`dbc`, design by contract:

- **`semantic-dbc`** `design/dbc/core`: contracts checked at run time.
  - `condition`: preconditions and postconditions of a call.
  - `invariant`: invariants a type keeps after every change.

`algebra`, algebraic abstractions: orders and structures named by the laws
their operations obey, the vocabulary of Algebra-Driven Design:

- **`semantic-algebra`** `design/algebra/core`: interfaces with their laws and
  the functions every lawful instance gets for free.
  - `relation`: equivalence, partial and total orders; `min`, `max`, `clamp`
    and sorting built on them.
  - `structure`: semigroups, monoids, groups and semilattices: combining
    values without knowing their type; `combineAll`, instances for numbers,
    durations, collections and maps.

### architecture

Styles of a service and the patterns it applies.

`style`: each style extends the one before it. Every style holds the same two
packages: `layer`, annotations that mark the layer of a class
(`@DomainLayer`, `@ApplicationLayer` and the rest), and `error`, the
exceptions of each layer.

- **`semantic-layered`** `architecture/style/layered/core`: presentation,
  application, domain and infrastructure tiers; each calls only the one below.
- **`semantic-onion`** `architecture/style/onion/core`: the domain in the
  centre, application services and infrastructure in rings around it.
- **`semantic-clean`** `architecture/style/clean/core`: entities, use cases,
  interface adapters and frameworks; dependencies point inward.
- **`semantic-explicit`** `architecture/style/explicit/core`: onion and clean
  combined, without ports and adapters.

`boundary`:

- **`semantic-boundary`** `architecture/boundary/core`: architectural
  boundaries: a class marked as part of a contract with other systems, the
  scope of that contract and the version it appeared in.

`pattern`: a module per pattern or per group of patterns. `persistence` groups the
patterns of how a service writes and reads its state.

- **`semantic-cqrs`** `architecture/pattern/persistence/cqrs/core`: separate models for
  writing and reading.
  - `command`: commands and their handlers.
  - `query`: queries and their handlers.
  - `bus`: command and query buses that dispatch a message to its handler.
- **`semantic-event-sourcing`** `architecture/pattern/persistence/event-sourcing/core`:
  state rebuilt from its events.
  - `store`: an append-only event store with optimistic concurrency.
  - `snapshot`: snapshots, so an aggregate is not replayed from its first
    event.
  - `projection`: read models built from the event stream.
- **`semantic-unit-of-work`** `architecture/pattern/persistence/unit-of-work/core`:
  changes committed atomically.
  - `tracking`: new, changed and removed objects tracked.
  - `commit`: all tracked changes written in one transaction.
- **`semantic-delivery`** `architecture/pattern/delivery/core`: delivery
  guarantees of a message. Outbox and inbox together give exactly-once.
  - `outbox`: transactional outbox: a message saved in the transaction of the
    change and relayed after it; at-least-once sending.
  - `inbox`: transactional inbox: the id of a received message saved in the
    transaction of its processing; at-most-once processing.
  - `idempotency`: idempotency keys of repeated client requests.
- **`semantic-coordination`** `architecture/pattern/coordination/core`:
  processes acting in step.
  - `saga`: orchestrated and choreographed sagas with compensating steps.
  - `lock`: a distributed lock with a lease and a fencing token.
- **`semantic-lifecycle`** `architecture/pattern/lifecycle/core`: a process
  from start to stop.
  - `startup`: graceful startup: components started in dependency order,
    traffic accepted only when ready.
  - `warmup`: caches, connection pools and JIT warmed up before readiness.
  - `health`: liveness and readiness health checks.
  - `shutdown`: graceful shutdown: new traffic refused, work in flight
    drained, then exit.
- **`semantic-caching`** `architecture/pattern/caching/core`: where a cache
  stands to its store: cache-aside, read-through, write-through, write-behind,
  refresh-ahead.
- **`semantic-resilience`** `architecture/pattern/resilience/core`: surviving
  a failing dependency.
  - `circuitbreaker`: circuit breaker: fail fast while a dependency is down.
  - `retry`: retries of transient faults.
  - `backoff`: exponential backoff with jitter between retries.
  - `timeout`: bounded waiting for a call.
  - `bulkhead`: isolated thread and connection pools.
  - `fallback`: a degraded answer when a call fails.
- **`semantic-traffic`** `architecture/pattern/traffic/core`: control of
  incoming load.
  - `ratelimit`: rate limiting: quotas per client, token bucket, sliding
    window.
  - `throttling`: the rate of requests smoothed.
  - `shedding`: load shedding: excess requests rejected under overload.
  - `backpressure`: back pressure: the producer slowed to the pace of the
    consumer.
- **`semantic-rollout`** `architecture/pattern/rollout/core`: features
  released in code, apart from a deployment.
  - `featureflag`: feature flags: a feature turned on or off at run time, for
    everyone or for a group of users.
  - `killswitch`: kill switch: an emergency breaker that turns a failing feature
    off at once, without a deployment.
  - `canary`: canary release in code: a feature turned on for a small share of
    users or traffic first, the share grown while the metrics stay healthy.
  - `darklaunch`: dark launch: a feature run, but not shown.
  - `experiment`: A/B experiments: users split into groups that see different
    variants, the variants compared by a metric.

Artifacts are named `semantic-<module>[-<adapter>]` and live in the group
`pro.vlprojects.semantic`. Packages follow the same path:
`pro.vlprojects.semantic.ddd.spring.data.jpa`.

Directories follow the structure above: a category is only a directory, a
module lies only at a leaf. The core of a module is `<path>/core`, a Spring
Data store of ddd is `design/ddd/spring/data/<store>`.

Built for Java 25, Kotlin 2.4 and Spring Boot 4.1. Licensed under Apache 2.0.
