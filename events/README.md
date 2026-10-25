# semantic-events

A transactional outbox for Kotlin and Java: an event is written in the same
transaction as the change that caused it and sent to a broker, Kafka or
RabbitMQ, after the commit. The family does not depend on `ddd` or Spring: the
code of a service sees a port, and a Spring module will take the events of
`publishEvent()` into the outbox.

The outbox takes an event as far as the broker and no further. An event is
published once the broker has acknowledged it; the broker then delivers it to
the consumers. The guarantee is at least once: if the broker took an event but
marking it published failed, the event is sent again. Each message carries the
id of its event, so a consumer can drop a duplicate.

The core holds the port and the model of the outbox so far. The store, the
relay, Jackson and the senders to Kafka and RabbitMQ come next.

## The core

Package `pro.vlprojects.semantic.events` of `semantic-events`:

| Type | What it is |
| --- | --- |
| `EventPublisher<E>` | the port: takes an event, or several in their order, within a transaction |
| `OutboxStatus` | `PENDING` until the broker has the event, then `PUBLISHED`; `ABANDONED` if the broker will never take it |
| `Backoff` | how long to wait after a failed attempt to send |
| `FixedBackoff` | the same delay after each attempt |
| `ExponentialBackoff` | a delay growing by a multiplier up to a max one, shortened by a random jitter |

A broker that is down is waited for as long as it takes: a backoff spaces the
attempts out and never gives up. An event that no broker will take, one that
cannot be written or is too large, is abandoned at once, so that it does not
hold back the events behind it.

```kotlin
class PlaceOrderService(private val events: EventPublisher<OrderPlaced>) {

    @Transactional
    fun place(order: Order) {
        events.publish(OrderPlaced(order.id))
    }
}

val backoff = ExponentialBackoff(initialDelay = 1.seconds, maxDelay = 5.minutes, jitter = 0.2)
```

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-events` | any JVM code: the port and the model of the outbox | nothing |

```
events/
└── core/                  semantic-events
```
