# semantic-resilience

Patterns that keep a service working when a dependency fails. Each pattern is a module of its own with
its own adapters, and a service declares only the patterns it uses.

This is the planned layout. So far only `Backoff`, still in `core`, and the windows of the circuit breaker
have code, and the adapters of each pattern are agreed when that pattern is built.

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

Every pattern except `backoff` is a `Policy`, and a combination of policies is a `Policy` too. The order
is what the calling code writes:

```kotlin
retry.around(breaker).execute { client.load() }
```

A deadline covers what it surrounds: inside `retry` it limits one attempt, outside it limits the whole
operation.

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
