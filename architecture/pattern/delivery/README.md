# semantic-delivery

Transactional outbox and inbox for Kotlin and Java. A service calls `Outbox.schedule` in its own
transaction; after the commit `Relay` calls an `OutboxHandler` at least once. A broker is one more
handler. The inbox drops the duplicates a consumer receives.

The core holds the contract of the outbox so far: its record, storage and routes. The relay and the
inbox come next; the other modules are still empty.

![Modules of delivery and what each one holds](../../../docs/assets/delivery-light.svg#gh-light-mode-only)
![Modules of delivery and what each one holds](../../../docs/assets/delivery-dark.svg#gh-dark-mode-only)

## The outbox contract

Packages `delivery.outbox` and `delivery.observability` of `semantic-delivery`:

| Type | What it is |
| --- | --- |
| `Outbox` | takes a payload in the transaction of the change that caused it |
| `OutboxRecord` | one payload for one handler: `key`, `handlerId`, the text of the payload, status, lease |
| `OutboxRecordRepository` | adds a record, claims a batch under a lease, closes a record by its lease token |
| `OutboxRecordAdministration` | counts records, returns `FAILED` to `NEW`, deletes |
| `OutboxPayloadSerializer` | turns a payload into text and back |
| `OutboxContextProvider` | adds context to a record, a trace or a tenant |
| `OutboxRouting` | finds where a broker handler sends a payload: the first matching `OutboxRoute` |
| `@TransactionalOutbox` | marks a payload that goes through the outbox, directly or on another annotation |
| `@IdempotencyKey` | marks the property that names the operation of a payload |
| `OutboxInstrumentation` | hears of a record scheduled, its handling started, handled and failed |

`key` orders records and `idempotencyKey` names the operation: an inbox carries an operation out once.
The latter is the key passed to `schedule`, else the `@IdempotencyKey` property, else the id of the record.
A `schedule` without a key takes the id of the record as its `key`, so such a record has no order.

`claim` takes one record per key, the earliest unfinished one, and order must hold for records with an
equal `createdAt`. A method that takes a token changes nothing and returns `false` once the lease is lost.

## Modules

| Directory | Artifact | What it is |
| --- | --- | --- |
| `core` | `semantic-delivery` | the model, the SPI and the processing, free of Spring |
| `jackson` | `semantic-delivery-jackson` | `OutboxPayloadSerializer` on JSON |
| `observability/micrometer` | `semantic-delivery-observability-micrometer` | timers and spans from the events of the core, optional |
| `test` | `semantic-delivery-test` | the contract every storage adapter runs |
| `spring/core` | `semantic-delivery-spring` | finding handlers, transactions, checks on start |
| `spring/jdbc` | `semantic-delivery-spring-jdbc` | `JdbcClient` and `SKIP LOCKED` |
| `spring/data/jpa` | `semantic-delivery-spring-data-jpa` | Spring Data JPA |
| `spring/data/mongodb` | `semantic-delivery-spring-data-mongodb` | `findAndModify` on a replica set |
| `spring/rabbitmq` | `semantic-delivery-spring-rabbitmq` | a handler that publishes to RabbitMQ, and its routes |
| `spring-boot/autoconfigure` | `semantic-delivery-spring-boot` | the `semantic.delivery` settings and `Relay` on a schedule |
| `spring-boot/actuator` | `semantic-delivery-spring-boot-actuator` | counts, pause of `Relay`, retry of `FAILED`, delete, health |
| `spring-boot/starter/jdbc` | `semantic-delivery-spring-boot-starter-jdbc` | one dependency for JDBC |
| `spring-boot/starter/jpa` | `semantic-delivery-spring-boot-starter-jpa` | one dependency for JPA |
| `spring-boot/starter/mongodb` | `semantic-delivery-spring-boot-starter-mongodb` | one dependency for MongoDB |
| `performance-test` | not published | throughput of the lease and of order by key |

## The path of a record

![A record from the transaction of a service to its handler](../../../docs/assets/delivery-flow-light.svg#gh-light-mode-only)
![A record from the transaction of a service to its handler](../../../docs/assets/delivery-flow-dark.svg#gh-dark-mode-only)

## Compared to Namastack Outbox

The modules follow Namastack Outbox and keep its handler model.

![Modules of Namastack Outbox and their counterparts here](../../../docs/assets/delivery-namastack-light.svg#gh-light-mode-only)
![Modules of Namastack Outbox and their counterparts here](../../../docs/assets/delivery-namastack-dark.svg#gh-dark-mode-only)

The core runs without Spring, records are claimed by `SKIP LOCKED` with a lease instead of partitions,
and the inbox has no counterpart there.
