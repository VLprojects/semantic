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
