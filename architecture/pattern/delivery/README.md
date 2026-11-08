# semantic-delivery

Transactional outbox and inbox for Kotlin and Java. A service calls `Outbox.schedule` in its own
transaction; after the commit `Relay` calls an `OutboxHandler` at least once. A broker is one more
handler. The inbox drops the duplicates a consumer receives.

The modules are empty so far: this is the layout the code will land in, module by module.

![Modules of delivery and what each one holds](../../../docs/assets/delivery-light.svg#gh-light-mode-only)
![Modules of delivery and what each one holds](../../../docs/assets/delivery-dark.svg#gh-dark-mode-only)

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
