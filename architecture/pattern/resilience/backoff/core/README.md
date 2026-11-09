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
    args:
      initial-delay: 1s
      delay-limit: 1m
      multiplier: 2
    jitter: 0.5
  instances:
    payments:
      type: fixed
      args:
        delay: 5s
      jitter: 0.3
    orders:
      args:
        initial-delay: 200ms
```

```kotlin
registry["payments"]               // fails when the name is unknown
registry.getOrDefault("inventory") // the default block for a name with no settings of its own
registry.default
```

`type` picks a `BackoffFactory` bean and `args` bind to its config class. `fixed` takes `delay` (`1s`) and
`exponential` takes `initial-delay` (`1s`), `delay-limit` (`1m`) and `multiplier` (`2`). To add an algorithm,
declare a `BackoffFactory` bean with its own `type`.

With nothing set, `type` is `exponential` and there is no jitter. An instance takes `type` and `jitter` from
`default` when it leaves them out, and the `args` of `default` when its type is the same as the type of
`default`. `jitter: 0` on an instance turns off the jitter of `default`.

Any `Backoff` bean is available by its bean name and replaces the settings of an instance with that name; a bean
named `default` replaces the default block. To change the registry from code, declare a
`BackoffRegistryCustomizer` bean: it gets the filled registry, settings and beans alike, and may go through
`names`, `register` and `replace`. Customizers run in `@Order`.

Wrong settings stop the application at start, with the name of the instance, even when a bean replaces it: an
unknown `type` or arg, a value that cannot be converted or that the `Backoff` being built refuses. An instance
named `default`, two factories of one `type` and an unknown field outside `args` stop it as well.
