# semantic-resilience-backoff

`Backoff` says how long to wait before an attempt, by its number. It only calculates the pause: whoever
calls it waits, repeats and decides when to stop.

```kotlin
FixedBackoff.ofSeconds(5)
ExponentialBackoff.of(initialDelay = Duration.ofSeconds(1), delayLimit = Duration.ofMinutes(1))
```

## Why jitter

When a dependency fails, its clients fail at the same moment. With the same pause they all come back at
the same moment too, and the burst of retries brings the dependency down again as soon as it recovers.

Jitter shortens each pause by a random share, so the retries spread out:

![Retries with the same pause and with a shortened pause](../../../../../docs/assets/resilience-jitter-light.svg#gh-light-mode-only)
![Retries with the same pause and with a shortened pause](../../../../../docs/assets/resilience-jitter-dark.svg#gh-dark-mode-only)

Any `Backoff` takes it. Use it whenever many clients or instances call the same dependency:

```kotlin
FixedBackoff.ofSeconds(5).jittered()
ExponentialBackoff.of().jittered(jitter = 1.0)
```

`jitter` is the largest share of the pause that can be taken away: with `0.5` a pause of 5 seconds becomes
one between 2.5 and 5 seconds, with `1.0` one between 0 and 5 seconds. The default is `0.5`. Jitter never
lengthens a pause, so the limit of `ExponentialBackoff` still holds.

## In Spring

`semantic-resilience-backoff-spring` passes a `Backoff` wherever Spring takes a `BackOff`:

```kotlin
RetryPolicy.builder().backOff(ExponentialBackoff.of().jittered().toSpring(maxAttempts = 3)).build()
```

`maxAttempts` is the number of pauses, after which Spring stops. Without it the `BackOff` never stops, which
suits a listener container that reconnects until the broker is back. `RetryPolicy` refuses `maxRetries` next
to a custom `BackOff`, so set `maxAttempts` there and in the error handler of Spring Kafka.

## Settings

`semantic-resilience-backoff-spring-boot` builds pauses from `application.yml` and exposes them as the
`BackoffRegistry` bean:

```yaml
semantic.resilience.backoff:
  default:
    type: exponential
    initial-delay: 1s
    delay-limit: 1m
    multiplier: 2
    jitter: 0.5
  instances:
    payments:
      type: fixed
      delay: 5s
      jitter: 0.3
    orders:
      initial-delay: 200ms
```

```kotlin
registry["payments"]               // fails when the name is unknown
registry.getOrDefault("inventory") // the default block for a name with no settings of its own
registry.default
```

With nothing set, the `default` block has the values shown above, and `delay` is `1s`; `jitter: 0` turns the
jitter off. An instance takes the fields it leaves out from `default`, including `type`. Any `Backoff` bean is
available by its bean name and replaces the settings of an instance with that name; a bean named `default`
replaces the default block. To change the registry from code, declare a `BackoffRegistryCustomizer` bean: it gets
the filled registry, settings and beans alike, and may go through `names`, `register` and `replace`.
Customizers run in `@Order`.

Wrong settings stop the application at start, with the name of the instance, even when a bean replaces it:
an unknown field or type, a value the `Backoff` being built refuses, a field of an instance that its type does
not use, such as `delay` on an exponential one, and an instance named `default`. The `default` block may hold
the fields of both types, since instances of either inherit them.
