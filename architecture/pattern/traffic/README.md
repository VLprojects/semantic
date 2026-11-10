# semantic-traffic

Rate limits for Kotlin and Java, applied the same way to incoming requests and to outgoing calls.
Incoming requests over the limit get 429. Outgoing calls wait for a permit, so a partner's quota is
never exceeded. `@RateLimit` puts the same limit on any method of a bean. The algorithms are the five from chapter 4, "Design a Rate Limiter", of Alex Xu's
"System Design Interview", volume 1. Storage sits behind an interface: memory by default, Redis for
a counter shared by all instances, and any other store that can compare a version.

This is the agreed design; only the `core` module exists so far, and it is empty.

![Modules of traffic and what each one holds](../../../docs/assets/traffic-light.svg#gh-light-mode-only)
![Modules of traffic and what each one holds](../../../docs/assets/traffic-dark.svg#gh-dark-mode-only)

## Modules

| Directory | Artifact | What it holds |
| --- | --- | --- |
| `core` | `semantic-traffic` | the contract, the five algorithms, a counter in memory; free of Spring |
| `observability/micrometer` | `semantic-traffic-observability-micrometer` | permits acquired and rejected, tagged by limiter name only |
| `spring/context` | `semantic-traffic-spring-context` | `@RateLimit` on methods of beans: a proxy without AspectJ, the key in SpEL |
| `spring/webmvc` | `semantic-traffic-spring-webmvc` | `RateLimitingFilter`: 429, `Retry-After`, `RateLimit` headers |
| `spring/web` | `semantic-traffic-spring-web` | `RateLimitingInterceptor` for `RestClient` |
| `spring/data/redis` | `semantic-traffic-spring-data-redis` | `RedisRateLimitRepository`: one counter for all instances |
| `spring-boot/autoconfigure` | `semantic-traffic-spring-boot` | the `semantic.traffic` settings, limiters by name, the filter, the interceptor and the proxy of `@RateLimit` |
| `spring-boot/starter` | `semantic-traffic-spring-boot-starter` | one dependency, counters in memory |
| `spring-boot/starter/redis` | `semantic-traffic-spring-boot-starter-redis` | one dependency, counters in Redis |

`spring/jdbc` is built when a service needs counters in a database. An adaptive limit of concurrent
requests, after Netflix concurrency-limits, gets a design of its own later.

## Abstractions

![Each task and the one abstraction that solves it](../../../docs/assets/traffic-duties-light.svg#gh-light-mode-only)
![Each task and the one abstraction that solves it](../../../docs/assets/traffic-duties-dark.svg#gh-dark-mode-only)

![The abstractions on the path of a call](../../../docs/assets/traffic-abstractions-light.svg#gh-light-mode-only)
![The abstractions on the path of a call](../../../docs/assets/traffic-abstractions-dark.svg#gh-dark-mode-only)

| Abstraction | What it does | What it does not do | After |
| --- | --- | --- | --- |
| `RateLimiter` | `tryAcquire(key, permits)` answers at once; the extension `acquire(key, permits, maxWait)` waits for a permit or throws `RateLimitExceededException` | knows neither the algorithm nor the storage | Guava `RateLimiter`, ASP.NET `RateLimiter` |
| `DefaultRateLimiter` | reads the counter, asks the algorithm, saves the new state by version; on a conflict it repeats, up to `max-attempts` times; applies `on-failure` when storage fails | holds no formula | Bucket4j `AbstractCompareAndSwapBasedProxyManager` |
| `RateLimitAlgorithm` | a sealed pure function: state, permits and time in, new state and decision out | reads no clock, touches no storage | Bucket4j `Bandwidth`, ASP.NET `TokenBucketRateLimiter` |
| `RateLimitState` | the state of one algorithm, one subtype each; the core turns it into bytes and back | | Bucket4j `RemoteBucketState` |
| `RateLimitCounter` | what storage keeps: key, state, version, `expiresAt` | | Spring Data `@Version` |
| `RateLimitRepository` | `findByKey`, and `save` that throws `RateLimitOptimisticLockException` when the version has changed | decides nothing | Spring Data `CrudRepository`, `OutboxRepository` |
| `InMemoryRateLimitRepository` | the default, on `ConcurrentHashMap.compute`; drops counters after `expiresAt` | is not shared by instances | Bucket4j `LockFreeBucket` |
| `RateLimitResponse` | `isAcquired`, `delay`, `remaining`, `retryAfter`, `resetAfter` | | Bucket4j `ConsumptionProbe`, Envoy `RateLimitResponse` |
| `RateLimiterRegistry` | `findByName(name)`: one limiter for the filter, the interceptor and `@RateLimit`; `InMemoryRateLimiterRegistry` by default | builds no limiter from settings, autoconfigure does | Resilience4j `RateLimiterRegistry`, Spring `CacheManager` |
| `@RateLimit` | on a method or a class: `limiter`, `key` in SpEL, `permits`, `maxWait` as text such as `"500ms"` | counts nothing | Spring `@ConcurrencyLimit`, SmallRye `@RateLimit` |
| `RateLimitBeanPostProcessor` | wraps a bean with `@RateLimit` in a proxy; an unknown limiter name stops the application at startup | needs no AspectJ | Spring `ConcurrencyLimitBeanPostProcessor`, `ScheduledAnnotationBeanPostProcessor` |
| `RateLimitMethodInterceptor` | evaluates the key, calls `acquire`, then the method; throws `RateLimitExceededException` | | Spring `ConcurrencyLimitInterceptor`, the key of `@Cacheable` |
| `@EnableRateLimiting` | turns the proxy on without Spring Boot; `order` places it among other proxies | | Spring `@EnableCaching`, `@EnableResilientMethods` |
| `RateLimitingFilter` | a `OncePerRequestFilter`: picks the limit by path, the key from `KeyResolver`, calls `RateLimitRejectionHandler` on rejection | counts nothing | ASP.NET `RateLimitingMiddleware` |
| `KeyResolver` | the key of a request: principal, remote address, a header | | Spring Cloud Gateway `KeyResolver` |
| `RateLimitRejectionHandler` | the answer to a rejected request; by default 429, `Retry-After` and a ProblemDetail | | ASP.NET `RateLimiterOptions.OnRejected` |
| `RateLimitingInterceptor` | a `ClientHttpRequestInterceptor` for `RestClient`: waits for a permit up to `max-wait` | does not retry a 429 of the partner, that is retry's job | .NET `ClientSideRateLimitedHandler` |

The algorithms are sealed because every storage has to save and restore the state of each of them.
A version instead of a lock works in any storage: `ConcurrentHashMap.compute` in memory, a Lua script
in Redis, `UPDATE … WHERE version = ?` in JDBC.

## One call

![Who calls whom on an incoming request](../../../docs/assets/traffic-flow-light.svg#gh-light-mode-only)
![Who calls whom on an incoming request](../../../docs/assets/traffic-flow-dark.svg#gh-dark-mode-only)

On a rejection the state does not change, so nothing is saved. Unlike Xu, `SlidingWindowLog` logs
only accepted requests, so the log of a key never grows past the limit, even under attack.

## Algorithms

![Five algorithms on one series of requests](../../../docs/assets/traffic-algorithms-light.svg#gh-light-mode-only)
![Five algorithms on one series of requests](../../../docs/assets/traffic-algorithms-dark.svg#gh-dark-mode-only)

| Algorithm | State | Strength | Cost | Fits |
| --- | --- | --- | --- | --- |
| `TokenBucket`, the default | tokens, time of the last refill | a burst up to `permit-limit`, then a steady pace | two numbers | APIs for clients |
| `LeakyBucket` | the theoretical arrival time (GCRA) | an even pace out; returns a `delay` instead of a rejection | one number | calls to a partner |
| `FixedWindow` | start of the window, count | the cheapest | twice the limit gets through at the edge of two windows | coarse quotas, a day or a month |
| `SlidingWindowLog` | the time of each accepted request | exact | up to `permit-limit` timestamps a key | small limits that must be exact |
| `SlidingWindowCounter` | two windows | nearly exact: the current window plus the previous one times the overlap | three numbers | large limits |

`LeakyBucket` is GCRA, not a queue in memory, so it works on a shared counter too: each call gets the
`delay` it has to wait, and the caller waits. Every algorithm takes `permit-limit` permits per
`window`, as ASP.NET names them.

## Incoming requests

![An incoming request in spring/webmvc](../../../docs/assets/traffic-incoming-light.svg#gh-light-mode-only)
![An incoming request in spring/webmvc](../../../docs/assets/traffic-incoming-dark.svg#gh-dark-mode-only)

A rejection is 429 with `Retry-After` in seconds (RFC 9110) and an `application/problem+json` body
(RFC 9457). With `headers: true` the filter also writes `RateLimit-Policy` and `RateLimit` after the
IETF draft `draft-ietf-httpapi-ratelimit-headers`; they are off by default while the draft is a draft.

## Outgoing calls

![Outgoing calls to a partner with and without a limiter](../../../docs/assets/traffic-outgoing-light.svg#gh-light-mode-only)
![Outgoing calls to a partner with and without a limiter](../../../docs/assets/traffic-outgoing-dark.svg#gh-dark-mode-only)

`RateLimitingInterceptor` calls `acquire(key, 1, maxWait)` with the partner as the key. If the wait
would be longer than `max-wait`, it throws `RateLimitExceededException` at once and the call is not
sent, as Guava `tryAcquire(timeout)` and Resilience4j `timeoutDuration` do.

## A method of a bean

![A call of a method with @RateLimit](../../../docs/assets/traffic-annotation-light.svg#gh-light-mode-only)
![A call of a method with @RateLimit](../../../docs/assets/traffic-annotation-dark.svg#gh-dark-mode-only)

`@RateLimit` limits any method of a bean, not only an HTTP request or a `RestClient` call: a message
listener, a scheduled task, a client of a partner's SDK.

```kotlin
@Component
class AcmeOrders(private val client: AcmeClient) {

    @RateLimit("partner-acme", key = "#tenantId", maxWait = "500ms")
    fun load(tenantId: String) = client.loadOrders(tenantId)
}
```

`RateLimitBeanPostProcessor` wraps the bean in a proxy as Spring 7 does for `@ConcurrencyLimit` and
`@Retryable`, with an `AnnotationMatchingPointcut` and no AspectJ. `RateLimitMethodInterceptor` takes the
limiter from `RateLimiterRegistry`, evaluates `key` as `@Cacheable` evaluates its key, and calls
`acquire(key, permits, maxWait)`. Without `key` the limiter keeps one counter. Without `maxWait` the call
does not wait: it either gets a permit at once or fails.

A rejection is `RateLimitExceededException` with `retryAfter`. On a controller method `spring/webmvc` turns
it into the same 429 as the filter does, through `RateLimitRejectionHandler`. Elsewhere it reaches the caller.

- The proxy is applied before the proxy of `@Retryable`, so the retry wraps the limit and every attempt
  takes a permit: a partner counts every attempt. `@EnableRateLimiting(order = …)` changes the place.
- An unknown limiter name stops the application at startup, as an invalid `@Scheduled` does, and not at
  the first call.
- A call from inside the same bean skips the proxy, as with `@Transactional`.
- `suspend` functions and reactive return types are not supported until a service needs them.

## A counter for several instances

![Two instances writing one counter by version](../../../docs/assets/traffic-shared-light.svg#gh-light-mode-only)
![Two instances writing one counter by version](../../../docs/assets/traffic-shared-dark.svg#gh-dark-mode-only)

The key in storage is `<key-prefix>:<limiter>:<key>`. A counter lives until `expiresAt`, the moment it
is full again; then storage drops it, by `PX` in Redis.

## Failures

![Storage failure, a hot key, expiry and a restart](../../../docs/assets/traffic-failures-light.svg#gh-light-mode-only)
![Storage failure, a hot key, expiry and a restart](../../../docs/assets/traffic-failures-dark.svg#gh-dark-mode-only)

| Case | What happens | Setting |
| --- | --- | --- |
| storage is down | the call goes through with a WARN and without `RateLimit` headers; `reject` turns it down | `on-failure: allow`, as Spring Cloud Gateway |
| version conflicts in a row | after `max-attempts` the same rule as a failure | `max-attempts: 8` |
| `@RateLimit` names an unknown limiter | the application does not start | |
| a key is no longer used | the counter is removed after `expiresAt` | |
| the process restarts | a counter in memory starts over; one in Redis stays | |
| clocks of instances differ | time comes from the `java.time.Clock` of the calling instance, so a skew of N ms moves a shared limit by N ms; documented, not corrected | |

## Wiring

![The same core wired without Spring and in Spring](../../../docs/assets/traffic-wiring-light.svg#gh-light-mode-only)
![The same core wired without Spring and in Spring](../../../docs/assets/traffic-wiring-dark.svg#gh-dark-mode-only)

Without Spring:

```kotlin
val limiter = DefaultRateLimiter("api", TokenBucket.of(100, Duration.ofMinutes(1)), InMemoryRateLimitRepository())

limiter.tryAcquire(key)
    .takeIf(RateLimitResponse::isAcquired)
    ?: return tooManyRequests()
```

In Spring the starter creates a limiter per entry of `limiters` and puts it into `RateLimiterRegistry`,
creates the filter for `incoming`, the interceptor for `outgoing` and `RateLimitBeanPostProcessor` for
`@RateLimit`, and picks Redis when `starter/redis` is on the classpath. Without Spring Boot,
`@EnableRateLimiting` and a `RateLimiterRegistry` bean are enough for `@RateLimit`:

```yaml
semantic.traffic:
  on-failure: allow            # allow | reject
  max-attempts: 8
  key-prefix: traffic
  headers: false               # RateLimit and RateLimit-Policy
  limiters:
    api:
      algorithm: token-bucket  # token-bucket | leaky-bucket | fixed-window | sliding-window-log | sliding-window-counter
      permit-limit: 100
      window: 1m
    partner-acme:
      algorithm: leaky-bucket
      permit-limit: 4
      window: 1s
  incoming:
    - path: /api/**
      limiter: api
      key: principal           # principal | remote-address | header:X-Tenant-Id | a KeyResolver bean
  outgoing:
    partner-acme:
      max-wait: 1500ms
```

Limiters are named as Resilience4j instances are (`resilience4j.ratelimiter.instances.<name>`).

## Extension points

![What can be replaced without touching the rest](../../../docs/assets/traffic-extensions-light.svg#gh-light-mode-only)
![What can be replaced without touching the rest](../../../docs/assets/traffic-extensions-dark.svg#gh-dark-mode-only)

Metrics carry the limiter name and never the key, so their cardinality stays bounded, as in
Resilience4j `TaggedRateLimiterMetrics`. WebFlux, an actuator endpoint and `@RateLimit` on `suspend`
and reactive methods are not built until a service needs them.
