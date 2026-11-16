# semantic-delivery: the polling container

The design of background delivery for the outbox. The code is not written yet; the pull requests at the end bring it
step by step.

## 1. The task

A record of the outbox is saved in the transaction of a business operation, and a background process has to deliver
it. `OutboxDispatcher.dispatch()` already delivers one batch: it takes up to `batch-size` records, sends them and saves
the outcome. Four questions are left:

- who calls `dispatch()` and when;
- how soon to call it again after a full, a partial or an empty batch;
- how many calls run in parallel and how their number follows the load;
- how to start and stop it all cleanly, with Spring and without it.

## 2. Requirements

1. It runs without Spring on the JDK alone. Spring comes in through an adapter.
2. The defaults are simple: one thread, sensible pauses, nothing to configure.
3. The algorithm of pauses can be replaced; an own one is a single lambda.
4. Optionally, the number of parallel polls grows under load up to a limit and falls when the load is gone.
5. Delivery stays at least once: neither a stop nor a crash loses a record.

## 3. The abstractions

![One task, one abstraction](../../../../docs/assets/delivery-polling-duties-light.svg#gh-light-mode-only)
![One task, one abstraction](../../../../docs/assets/delivery-polling-duties-dark.svg#gh-dark-mode-only)

Each task is solved in exactly one place, so a changed requirement changes one abstraction.

What happens to 10 000 records with `batch-size: 100` and `max-concurrency: 4`:

1. `OutboxPollingContainerLifecycle` calls `start()` when the context starts.
2. `OutboxPollingContainer` creates one `OutboxPoller` and puts it into a `ScheduledExecutorService`.
3. `OutboxPoller` calls `OutboxDispatcher.dispatch()`, which takes 100 records, delivers them and returns 100.
4. `OutboxPoller` asks `ActiveIdlePollingStrategy`. The batch was full, the pause is 0, the next batch starts at once.
5. After 10 full batches in a row the container adds a second poller, 10 s later a third, then a fourth. Four threads
   take batches from then on.
6. The records run out and batches are no longer full. The strategy returns a pause of 1 s, and once every 60 s the
   container removes a poller until one is left.

![The abstractions](../../../../docs/assets/delivery-polling-abstractions-light.svg#gh-light-mode-only)
![The abstractions](../../../../docs/assets/delivery-polling-abstractions-dark.svg#gh-dark-mode-only)

| Abstraction | What it is | What it does | What it does not do | Taken from |
| --- | --- | --- | --- | --- |
| `OutboxDispatcher` | class, exists | delivers one batch: takes records, sends them, saves the outcome; returns how many it took | knows nothing of time and threads | — |
| `OutboxPollingStrategy` | fun interface | decides from the outcome of a batch when to poll again | runs no threads and keeps no state | Spring Messaging `ReconnectStrategy`, Namastack `OutboxPollingTrigger` |
| `OutboxPollingContext` | class | the facts of a batch: `dispatched`, `batchSize`, `previousDurationBeforePolling`, `isBatchFull()` | — | Spring `TriggerContext` |
| `OutboxPoller` | internal class | runs one loop: batch, strategy, pause, batch again; a failed batch does not stop the loop | does not decide how many pollers there are | Spring Integration `AbstractPollingEndpoint` |
| `OutboxPollingContainer` | class | runs delivery in the background by polling: `start()`, `stop()`, `isRunning()`, `wakeup()`; keeps from `concurrency` to `max-concurrency` pollers on a `ScheduledExecutorService` | delivers nothing itself and counts no pauses | Spring JMS `DefaultMessageListenerContainer`, Spring AMQP `SimpleMessageListenerContainer` |
| `OutboxPollingContainerLifecycle` | class in `semantic-delivery-spring` | a `SmartLifecycle` that starts and stops the container with the context | holds no polling logic | Spring Boot `WebServerGracefulShutdownLifecycle` |

**Why a container.** The class works like a listener container of Spring: it polls a source in a loop, keeps from
`concurrency` to `max-concurrency` consumers, scales them and has `start` and `stop`. The code of the application inside
it is an `OutboxDeliverer`, called through `OutboxDispatcher`.

**Why no interface.** The library has no way to deliver other than polling. PostgreSQL `LISTEN/NOTIFY` loses a
notification when nobody listens at the commit, so Oban, GoodJob and River keep polling as the safety net and use the
notification only to wake it up. Reading the log of the database (Debezium, Eventuate CDC) runs as a separate process,
and then the container is switched off. Section 7 describes the wake-up of polling.

`OutboxPoller` is `internal`: it is not public API and can still be renamed freely.

## 4. One poller

![One poller](../../../../docs/assets/delivery-polling-poller-light.svg#gh-light-mode-only)
![One poller](../../../../docs/assets/delivery-polling-poller-dark.svg#gh-dark-mode-only)

1. `dispatch()` takes and delivers a batch and returns the count `n`.
2. The strategy returns a pause `delay` from `n` and the last pause.
3. The poller puts itself into the queue of the executor with `schedule(this, delay)` and frees the thread.

While a poller waits, its thread is free: eight idle pollers do not hold eight sleeping threads, because the pause waits
in the queue of the executor, not in `Thread.sleep`.

- **`dispatch()` throws**, for example when the database is down. The failure is logged at the level set in `logging`,
  the poller waits as after an empty batch and tries again.
- **`stop()` is called.** The poller no longer puts itself into the queue. It finishes the batch it started, see
  section 9.

## 5. Polling strategies

![Polling strategies](../../../../docs/assets/delivery-polling-strategies-light.svg#gh-light-mode-only)
![Polling strategies](../../../../docs/assets/delivery-polling-strategies-dark.svg#gh-dark-mode-only)

```kotlin
public fun interface OutboxPollingStrategy {

    public fun calculateDurationBeforePolling(context: OutboxPollingContext): Duration
}
```

- The name says what is calculated: the duration before the next polling. As with `Backoff`, facts in, a pause out.
- A strategy keeps no state; the poller passes it the last pause. One instance is safe to share between parallel
  pollers. The `OutboxPollingTrigger` of Namastack keeps state and works only with one polling thread.
- The context is a class, not a list of parameters, so a new field does not break an implementation, as with Spring
  `TriggerContext`.

| Strategy | Pause | When to choose | Taken from |
| --- | --- | --- | --- |
| `FixedPollingStrategy(interval)` | always `interval` | a steady load on the database | Namastack `FixedPollingTrigger`, Spring `PeriodicTrigger` |
| `ActiveIdlePollingStrategy(activeInterval, idleInterval)` | `activeInterval` after a full batch (0 by default), `idleInterval` otherwise | **the default**: a burst goes without pauses, an idle database is polled once per `idleInterval` | Spring Integration `SimpleActiveIdleReceiveMessageAdvice` |
| `AdaptivePollingStrategy(minInterval, maxInterval, multiplier)` | a full batch divides the pause by `multiplier` (2 by default), a batch of a quarter or less multiplies it, within min..max | rare records and fewer queries | Namastack `AdaptivePollingTrigger` |

An own algorithm is `OutboxPollingStrategy { context -> ... }`.

## 6. Scaling (optional)

![Scaling](../../../../docs/assets/delivery-polling-scaling-light.svg#gh-light-mode-only)
![Scaling](../../../../docs/assets/delivery-polling-scaling-dark.svg#gh-dark-mode-only)

Scaling is off while `max-concurrency` equals `concurrency`; both are 1 by default. With a higher `max-concurrency` the
container changes the number of pollers by the rule of Spring AMQP:

- **+1 poller** after N full batches of a poller in a row, at most once per interval and up to `max-concurrency`; in
  Spring AMQP `consecutiveActiveTrigger` is 10 and `startConsumerMinInterval` is 10 s;
- **−1 poller** after M partial batches of a poller in a row, that poller stops, at most once per interval and down to
  `concurrency`; in Spring AMQP `consecutiveIdleTrigger` is 10 and `stopConsumerMinInterval` is 60 s.

A partial batch is the signal because it means there are more pollers than records coming in. Going down is slower than
going up, 60 s against 10 s, so the number of pollers does not swing.

![Several pollers and instances](../../../../docs/assets/delivery-polling-parallel-light.svg#gh-light-mode-only)
![Several pollers and instances](../../../../docs/assets/delivery-polling-parallel-dark.svg#gh-dark-mode-only)

Parallel pollers need no coordination: each takes its own batch through `findAndModifyScheduled` (`SKIP LOCKED` or
`findAndModify`), and no record gets into two batches. For the same reason several instances of the application need no
leader.

## 7. Wake-up (optional)

![Polling and wake-up](../../../../docs/assets/delivery-polling-flow-light.svg#gh-light-mode-only)
![Polling and wake-up](../../../../docs/assets/delivery-polling-flow-dark.svg#gh-dark-mode-only)

Without a wake-up a record created while idle waits up to `idle-interval`, 1 s. With it, the application calls
`OutboxPollingContainer.wakeup()` after the commit, and a pausing poller starts a batch at once. Polling by the strategy
stays; the wake-up only gets ahead of it. Gruelbox `TransactionOutbox` and Wolverine work this way: they send right after
the commit and polling picks up whatever is left.

Who calls `wakeup()`:

- with Spring, the adapter in `semantic-delivery-spring` registers `TransactionSynchronization.afterCommit` when a
  record is created; a rollback sends no signal;
- without Spring, the application after its own commit.

![Wake-up under load](../../../../docs/assets/delivery-polling-signals-light.svg#gh-light-mode-only)
![Wake-up under load](../../../../docs/assets/delivery-polling-signals-dark.svg#gh-dark-mode-only)

Thousands of commits a second do not overload the database:

1. **Signals merge.** `wakeup()` sets a flag rather than queues a task: a thousand calls between two polls give one
   wake-up, as several calls of JDK `Selector.wakeup()` act as one.
2. **At most once per `cooldown`.** After a wake-up the next one comes no sooner than `polling.wakeup.cooldown`, 100 ms
   by default, so the wake-up adds at most 10 queries a second per instance. River `FetchCooldown` is 100 ms as well.
3. **One pausing poller wakes up.** The others keep waiting, so the pollers do not hit the database all at once.
4. **A busy poller needs no signal.** With full batches the strategy makes no pauses, and the signal is skipped.
5. **Scaling does not depend on signals.** Only full batches in a row add pollers, as in section 6; a signal creates no
   pollers and no threads.

The signal lives in the memory of one instance. Records of another instance or a lost signal are picked up by regular
polling, so at least once does not depend on the wake-up.

```yaml
semantic.delivery.outbox.polling:
  wakeup:
    enabled: false
    cooldown: 100ms
```

## 8. Wiring

![Without Spring and with Spring](../../../../docs/assets/delivery-polling-wiring-light.svg#gh-light-mode-only)
![Without Spring and with Spring](../../../../docs/assets/delivery-polling-wiring-dark.svg#gh-dark-mode-only)

Without Spring:

```kotlin
val executor = Executors.newScheduledThreadPool(properties.polling.maxConcurrency)
val container = OutboxPollingContainer(dispatcher, executor, ActiveIdlePollingStrategy.of(), properties.polling)

container.start()
container.stop()
```

The application passes the executor and closes it: the container never shuts down an executor it did not create, as
Spring does with executors passed to it.

With Spring the starter is enough. The auto-configuration:

- creates `OutboxPollingContainer` on its own `ThreadPoolTaskScheduler` with `max-concurrency` threads named
  `outbox-poller-`;
- registers `OutboxPollingContainerLifecycle`, which calls `stop()` when the context closes and waits up to
  `shutdown-timeout`.

To keep `@Scheduled`, switch the container off with `polling.enabled: false` and call `dispatcher.dispatch()` from an own
method; strategies and scaling then take no part.

![What can be replaced](../../../../docs/assets/delivery-polling-extensions-light.svg#gh-light-mode-only)
![What can be replaced](../../../../docs/assets/delivery-polling-extensions-dark.svg#gh-dark-mode-only)

Settings and their defaults:

```yaml
semantic.delivery.outbox:
  batch-size: 100
  polling:
    enabled: true
    strategy:
      active-idle:              # or fixed, adaptive, an own type
        active-interval: 0s
        idle-interval: 1s
    concurrency: 1
    max-concurrency: 1
    shutdown-timeout: 30s
```

The key of the block under `polling.strategy` names an `OutboxPollingStrategyFactory` bean by its class without the
suffix, in kebab case: `ActiveIdlePollingStrategyFactory` is `active-idle`, as Spring Cloud Gateway names its filter
factories. The fields under the key bind to the type argument of the factory, its `Config`, resolved the way Spring
resolves the event of an `ApplicationListener`. `fixed` takes `interval` (`1s`), `active-idle` takes `active-interval`
(`0s`) and `idle-interval` (`1s`), `adaptive` takes `min-interval` (`100ms`), `max-interval` (`10s`) and `multiplier`
(`2`). The factories come in PR 5 to `semantic-delivery-spring-boot`; an own strategy is a factory class with its own
`Config`, declared as a bean. Without Spring the application passes the strategy to the constructor of the container.
`concurrency` and `max-concurrency` follow Spring Boot (`spring.rabbitmq.listener.simple.concurrency`,
`max-concurrency`).

## 9. Graceful shutdown

![Graceful shutdown](../../../../docs/assets/delivery-polling-shutdown-light.svg#gh-light-mode-only)
![Graceful shutdown](../../../../docs/assets/delivery-polling-shutdown-dark.svg#gh-dark-mode-only)

`OutboxPollingContainer.stop()`:

1. The state moves from `RUNNING` to `STOPPING`, and no new batch starts.
2. Pausing pollers leave the queue of the executor through `ScheduledFuture.cancel(false)`; they have no batch to wait
   for.
3. Pollers with a started batch finish it. Threads are not interrupted: an interrupt in the middle of a send to a broker
   adds duplicates and saves no time.
4. `stop()` waits for them up to `shutdown-timeout`, 30 s by default, the same as
   `spring.lifecycle.timeout-per-shutdown-phase`.
5. When the time is out, a WARN is logged and the state becomes `STOPPED`. The records of an unfinished batch stay
   `PROCESSING` until a background job returns them by `staleness.processing`. No record is lost.

Namastack stops the same way: `cancel(false)`, a wait with `shutdownTimeout`, a WARN on timeout.

With Spring `OutboxPollingContainerLifecycle` implements `SmartLifecycle.stop(Runnable)` in a phase above connection pools
and broker clients, so delivery stops before the database and the broker close. Spring Kafka containers use
`DEFAULT_PHASE = Integer.MAX_VALUE - 100`.

A batch of 100 records on a slow broker may not fit into `shutdown-timeout`, and its undelivered records then wait 10
minutes for the background job. An option to decide on: in `STOPPING` the dispatcher finishes the current record and
returns the rest of the batch to `PENDING` at once, as Spring Kafka `ContainerProperties.stopImmediate` stops after the
current record and Quartz `releaseAcquiredTrigger` returns what was acquired but not started. The counterpart of
`acquireScheduledForDelivery` would be `OutboxService.releaseAcquired`. `dispatch()` would need a stop signal, which
changes its API, so this is an open question.

## 10. Guarantees and limits

![A failed batch, a stop and a crash](../../../../docs/assets/delivery-polling-failures-light.svg#gh-light-mode-only)
![A failed batch, a stop and a crash](../../../../docs/assets/delivery-polling-failures-dark.svg#gh-dark-mode-only)

- **At least once.** If the process stops in the middle of a batch or `stop()` does not wait it out, the records stay
  `PROCESSING` until a background job returns them by `staleness.processing`. A record may be sent twice, never lost.
- **Order.** With more than one poller or instance the order by `orderingKey` is not guaranteed yet.
- **Pool size.** The pool needs at least `max-concurrency` threads, or the extra pollers wait in its queue. The
  auto-configuration sizes it; without Spring `start()` checks it.

## 11. Deferred

| What | Where it goes |
| --- | --- |
| Delivery without polling | outside the library: CDC through Debezium Outbox Event Router with the container off; an interface above the container only if MongoDB change streams get an implementation |
| A replaceable scaling rule | an interface once a second rule is needed |

## 12. Pull requests

| PR | What | Size |
| --- | --- | --- |
| 1 | `OutboxPollingStrategy`, `OutboxPollingContext`, three implementations, unit tests | ~250 lines, no threads |
| 2 | `OutboxPollingContainer` with a fixed number of pollers, `OutboxPoller`, `start`, `stop`, `shutdown-timeout`, logging of a failed poll | ~350 lines |
| 3 | scaling from `concurrency` to `max-concurrency` | ~200 lines |
| 4 | wake-up: `wakeup()`, merged signals, `cooldown` | ~150 lines |
| 5 | auto-configuration with the strategy factories and their `Config` bound to `polling.strategy`, `OutboxPollingContainerLifecycle` in the Spring modules, `afterCommit` for the wake-up | separate |

## 13. Open questions

1. The scaling rule: settings only, as in Spring AMQP, or an interface from the start?
2. Is `ActiveIdlePollingStrategy` the default?
3. Is `OutboxPoller` the name of one polling loop, as the poller of Spring Integration?
4. Return the unstarted records of a batch to `PENDING` on stop (`stopImmediate`, `releaseAcquired`) in the second PR or
   later?
