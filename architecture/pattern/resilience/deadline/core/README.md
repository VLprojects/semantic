# semantic-resilience-deadline

`Deadline` is a `Policy` that limits in time what it surrounds, also known as a timeout. It runs the
action in a virtual thread and interrupts it at the limit.

```kotlin
val deadline = Deadline.of("payments", Duration.ofSeconds(2))

val receipt = deadline.execute { gateway.charge(order) }
```

Past the limit the caller gets `DeadlineExceededException` at once. An action finished in time gives its
result or its own exception unchanged.

## An action that ignores the interrupt

A JVM cannot kill a thread. Such an action runs to its end, nobody waits for it and its result is dropped.
`DeadlineListener.onOverrun` reports when it ended, so `onExpired` minus `onOverrun` is how many calls are
still running past their limit.

## What the new thread does not see

- A value in a `ThreadLocal`, such as MDC or a security context, unless a `ThreadFactory` passed to `Deadline`
  carries it over. The factory is called in the thread of the caller.
- A transaction bound to the thread of the caller. Limit a query with the timeout of the database.

## Listeners

Listeners run in the thread of the call and must not block. A failing listener does not change the result:
its exception goes to the uncaught exception handler of the thread.
