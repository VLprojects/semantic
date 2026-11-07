# semantic-edd

Event-driven design for Kotlin and Spring, after the events of Spring Modulith.
Planned: no module is in the build yet, the map below is the design to start from.

- An event is any object. The library wraps it in a publication of its own: the
  id, the time, the listener, the status and the attempts.
- A registry of publications keeps one record per event and listener in the
  transaction that published the event, so a listener that failed or never ran
  is called again.
- Sending outside is one more listener. It takes the name and the version of an
  event from `@Integrated` and hands a message to a sender of
  [`delivery`](../../architecture/pattern/delivery/core); the outbox, the inbox
  and the senders to a broker live there and know nothing of events.
- `semantic-ddd` does not depend on edd. Its Spring Data modules hand the events
  of aggregates to `ApplicationEventPublisher`, and edd takes them from there.

## Modules

A plate is a module: its directory, its artifact, its analogue in Spring
Modulith, its abstractions and where it stops. A dashed outline marks a module
that is not there yet.

![The modules of edd and delivery, their abstractions and links, by the level of the framework they need](../../docs/assets/edd-light.svg#gh-light-mode-only)
![The modules of edd and delivery, their abstractions and links, by the level of the framework they need](../../docs/assets/edd-dark.svg#gh-dark-mode-only)

The script is [`docs/diagrams/edd.py`](../../docs/diagrams/edd.py); it will move
into `build.py` with the other module maps.
