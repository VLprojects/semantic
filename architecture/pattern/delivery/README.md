# semantic-delivery

Delivery guarantees of a message for Kotlin and Java. The module does not
depend on `ddd` or Spring: the code of a service sees a port, and the adapters
plug it into a store and a broker.

The core holds the port and the model of the outbox so far. Its store, relay
and senders to Kafka and RabbitMQ come next, the inbox after them.

## The outbox

A transactional outbox: an event is written in the same transaction as the
change that caused it and sent to a broker, Kafka or RabbitMQ, after the
commit.

The outbox takes an event as far as the broker and no further. An event is
published once the broker has acknowledged it; the broker then delivers it to
the consumers. The guarantee is at least once: if the broker took an event but
marking it published failed, the event is sent again. Each message carries the
id of its event, so the inbox of a consumer can drop a duplicate.

Package `pro.vlprojects.semantic.delivery.outbox` of `semantic-delivery`:

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

val backoff = ExponentialBackoff(
    initialDelay = Duration.ofSeconds(1),
    maxDelay = Duration.ofMinutes(5),
    jitter = 0.2,
)
```

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-delivery` | any JVM code: the port and the model of the outbox | nothing |

```
architecture/pattern/delivery/
└── core/                  semantic-delivery
```
