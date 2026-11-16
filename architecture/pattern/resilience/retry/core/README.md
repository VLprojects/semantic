# semantic-resilience-retry

Repeats a failed call. `Retry` is a `Policy`, so it combines with the other patterns.

```kotlin
val retry = Retry.of(
    name = "profile",
    rule = RetryRule.of(
        backoff = ExponentialBackoff.of().jittered(),
        attemptLimit = 4,
        timeLimit = Duration.ofSeconds(10),
        condition = RetryCondition.including(IOException::class),
    ),
)

val profile = retry.execute { http.get(id) }
```

- `attemptLimit` counts the first call. After the last attempt the original failure is thrown.
- A retry stops before a delay that does not fit `timeLimit`. A running attempt is not cut off, that is the job of a deadline.
- `Error`, `InterruptedException` and `CancellationException` are never repeated.
- An interrupted thread starts no new attempt and keeps its interrupt flag.
- `RetryRule.decide` is pure, so a caller that keeps attempts elsewhere can use it without `Retry`.
