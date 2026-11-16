# Messaging

`semantic-messaging` — the envelope a message travels in on its way between
applications. It knows nothing about a broker, a format or a delivery guarantee:
`infrastructure` holds the mechanisms that connect an application to the outside
world, with no guarantees and no business meaning. Guarantees live in
`architecture/pattern`.

## Envelope

`Envelope` wraps a message of any type and adds what a transport and its
middleware need to know about it. It is immutable: every change returns a new
envelope.

| Member | What it does |
| --- | --- |
| `messageId` | identifies this message |
| `idempotencyKey` | identifies the effect the message asks for |
| `message` | the payload |
| `stamps` | metadata, oldest first |
| `with(stamps)` | adds stamps to the end |
| `withoutAll<S>()` | removes every stamp of type `S` and its subtypes |
| `all<S>()` | returns every stamp of type `S` and its subtypes, oldest first |
| `last<S>()` | returns the newest stamp of type `S`, or `null` |

## Two identities

`messageId` and `idempotencyKey` answer different questions, so neither replaces
the other.

- `messageId` is unique for each message. Two messages sent for the same reason
  have two ids. Use it to find one message in logs and traces.
- `idempotencyKey` is shared by every message that asks for the same effect. It
  comes from the business operation, so a repeated operation yields the same
  key. Use it to recognise a repeat.

## Stamps

`Stamp` is a marker for metadata attached to an envelope: an attempt number, a
routing hint, a trace context. A stamp type may occur many times, and the order
of addition is kept.

`LocalStamp` is a `Stamp` that makes sense only inside the current process, such
as a reference to the connection a message arrived on. Remove such stamps
before the envelope leaves the process, with `withoutAll<LocalStamp>()`.

## Example

```kotlin
class OrderPaid(val orderId: Long)

class Attempt(val number: Int) : Stamp

class ReceivedOn(val connection: String) : LocalStamp

val envelope = Envelope(
    messageId = UUID.randomUUID(),
    idempotencyKey = "order-42-paid",
    message = OrderPaid(42),
)
    .with(Attempt(1), ReceivedOn("connection-7"))
    .with(Attempt(2))

envelope.last<Attempt>()?.number // 2
envelope.all<Attempt>().size // 2

val outgoing = envelope.withoutAll<LocalStamp>()
outgoing.last<ReceivedOn>() // null
```
