# semantic-delivery

Delivery of a message for Kotlin and Java: an outbox sends it after the commit
that wrote it, an inbox at the other end carries it out once. A message is
anything, not only an event, and the destination is anything a sender reaches:
a broker, another service, a module of the same application. The family does
not depend on `ddd`, `edd`, Spring or the clients of a broker.

The core holds the message and the outbox so far. Their sender, store and
relay come next, the inbox after them.

## The core

Package `pro.vlprojects.semantic.delivery` of `semantic-delivery`:

| Type | What it is |
| --- | --- |
| `Message` | what is delivered: its `messageId`, its `idempotencyKey` and its `payload` |
| `outbox.Outbox` | takes a message for a destination in the transaction of the change that caused it |

A message has two identities. The `messageId` names this one message: a
message put twice is still one. The `idempotencyKey` names the operation it
carries: two messages of one operation, a command sent again or an event sent
to two destinations, share it, and the inbox of a consumer carries the
operation out once.

The destination is not part of a message: a message is put into a channel, and
the same message may go to several.

```kotlin
class OrderPlacedMessage(
    override val messageId: String,
    override val idempotencyKey: String,
    override val payload: OrderPlaced,
) : Message

class PlaceOrderService(private val outbox: Outbox) {

    @Transactional
    fun place(order: Order) {
        val message = OrderPlacedMessage(
            messageId = UUID.randomUUID().toString(),
            idempotencyKey = order.id.toString(),
            payload = OrderPlaced(order.id),
        )
        outbox.put("orders", message)
    }
}
```

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-delivery` | any JVM code: the message and the outbox | nothing |

```
architecture/pattern/delivery/
└── core/                  semantic-delivery
```
