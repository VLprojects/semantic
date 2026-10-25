# semantic-events

Publishing of events and a transactional outbox for Kotlin and Java. An event
is written in the same transaction as the change that caused it and delivered
to each of its consumers after the commit, again and again until it is handled
or given up. The family does not depend on `ddd` or Spring: the code of a
service sees a port, and a Spring module will plug the outbox into
`@TransactionalEventListener`.

The core holds the port and the model of a delivery so far. The store, the
dispatcher and the Spring modules come next.

## The core

Package `pro.vlprojects.semantic.events` of `semantic-events`:

| Type | What it is |
| --- | --- |
| `EventPublisher<E>` | the port: takes an event, or several in their order, within a transaction |
| `ConsumerId` | the id of a consumer, given explicitly and kept when its method is renamed |
| `DeliveryStatus` | `PENDING`, `COMPLETED`, `FAILED` for an event nobody knows, `ABANDONED` once the attempts run out |
| `RetryPolicy` | how long to wait after a failed attempt, or `null` to give up |
| `FixedRetryPolicy` | the same delay after each attempt |
| `ExponentialRetryPolicy` | a delay growing by a multiplier up to a max one, shortened by a random jitter |

An event is stored once and delivered to each consumer on its own: a consumer
that failed is retried without the others getting the event again. A consumer
is found by its id, not by the signature of its method, so renaming a method
loses nothing.

```kotlin
class PlaceOrderService(private val events: EventPublisher<OrderPlaced>) {

    @Transactional
    fun place(order: Order) {
        events.publish(OrderPlaced(order.id))
    }
}

val retries = ExponentialRetryPolicy(
    initialDelay = 1.seconds,
    maxDelay = 5.minutes,
    maxAttempts = 10,
    jitter = 0.2,
)
```

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-events` | any JVM code: the port, deliveries and retries | nothing |

```
events/
└── core/                  semantic-events
```
