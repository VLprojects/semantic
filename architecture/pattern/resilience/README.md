# semantic-resilience

Patterns that keep a service working when a dependency fails. Each pattern is a module of its own with
its own adapters, and a service declares only the patterns it uses.

This is the planned layout. So far only `Policy`, `Backoff` and the windows of the circuit breaker have code,
and the adapters of each pattern are agreed when that pattern is built.

![Patterns of resilience and the adapters of each one](../../../docs/assets/resilience-light.svg#gh-light-mode-only)
![Patterns of resilience and the adapters of each one](../../../docs/assets/resilience-dark.svg#gh-dark-mode-only)

## Modules

| Directory | Artifact | What it holds |
| --- | --- | --- |
| `core` | `semantic-resilience` | `Policy` and how policies combine; depends on no pattern |
| `backoff/core` | `semantic-resilience-backoff` | how long to wait between attempts |
| `retry/core` | `semantic-resilience-retry` | repeats a failed call |
| `circuit-breaker/core` | `semantic-resilience-circuit-breaker` | stops calling a dependency that is down |
| `deadline/core` | `semantic-resilience-deadline` | a limit in time on what it surrounds |
| `bulkhead/core` | `semantic-resilience-bulkhead` | a compartment of concurrent calls per dependency |
| `fallback/core` | `semantic-resilience-fallback` | a spare answer on failure |

An adapter lives under its pattern: `<pattern>/observability/micrometer`, `<pattern>/spring/core`,
`<pattern>/spring-boot/autoconfigure`, `<pattern>/spring-boot/actuator`, `<pattern>/spring-boot/starter`.

## Combining patterns

`Retry`, `CircuitBreaker`, `Deadline` and `Bulkhead` are each a `Policy`, and a combination of policies is
a `Policy` too. The first policy is the outermost:

```kotlin
val resilient = Policy.of(deadline, breaker, retry, bulkhead)

resilient.execute { client.load() }
```

The library does not enforce an order. This is the recommended one, from the outermost:

```text
fallback
└─ deadline          one limit on the whole operation
   └─ circuit breaker
      └─ retry
         └─ bulkhead
            └─ the call
```

`backoff` runs nothing, so it is not a `Policy`. `Fallback` is not one either: its spare answer has a type
of its own. It takes a ready policy and so always stays the outermost:

```kotlin
Fallback(Profile.EMPTY, resilient).execute { client.load() }
```

The other forms of combining and the reasons for this order are in the [README of `core`](core/README.md).

## Modules of each pattern

### Backoff

![Modules of backoff](../../../docs/assets/resilience-backoff-light.svg#gh-light-mode-only)
![Modules of backoff](../../../docs/assets/resilience-backoff-dark.svg#gh-dark-mode-only)

### Retry

![Modules of retry](../../../docs/assets/resilience-retry-light.svg#gh-light-mode-only)
![Modules of retry](../../../docs/assets/resilience-retry-dark.svg#gh-dark-mode-only)

### Circuit breaker

![Modules of circuit-breaker](../../../docs/assets/resilience-circuit-breaker-light.svg#gh-light-mode-only)
![Modules of circuit-breaker](../../../docs/assets/resilience-circuit-breaker-dark.svg#gh-dark-mode-only)

### Deadline

![Modules of deadline](../../../docs/assets/resilience-deadline-light.svg#gh-light-mode-only)
![Modules of deadline](../../../docs/assets/resilience-deadline-dark.svg#gh-dark-mode-only)

### Bulkhead

![Modules of bulkhead](../../../docs/assets/resilience-bulkhead-light.svg#gh-light-mode-only)
![Modules of bulkhead](../../../docs/assets/resilience-bulkhead-dark.svg#gh-dark-mode-only)

### Fallback

![Modules of fallback](../../../docs/assets/resilience-fallback-light.svg#gh-light-mode-only)
![Modules of fallback](../../../docs/assets/resilience-fallback-dark.svg#gh-dark-mode-only)

## Elsewhere

Rate limiting and load shedding are in `traffic`, a cache is in `caching`. `@Retryable` and
`@ConcurrencyLimit` come with Spring Framework 7 and are not repeated here.
