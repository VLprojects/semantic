# Plan of traffic

A working file: the last pull request of the plan removes it together with `traffic-plan-*.svg`.
The design is in [README.md](README.md).

![Pull requests in sequence and in parallel](../../../docs/assets/traffic-plan-light.svg#gh-light-mode-only)
![Pull requests in sequence and in parallel](../../../docs/assets/traffic-plan-dark.svg#gh-dark-mode-only)

## Stage 1, in sequence

| PR | What it holds | Size |
| --- | --- | --- |
| 1 | the contract: `RateLimiter.allow(key, cost)`, `RateLimitDecision` | ~50 lines |
| 2 | `RateLimitStrategy`, `RateLimitState`, `RateLimitResult`, `TokenBucket` and its state; unit tests | ~200 lines |
| 3 | `RateLimitCounter`, `RateLimitRepository`, `RateLimitOptimisticLockException`, `InMemoryRateLimitRepository`; unit tests on a `Clock` | ~150 lines |
| 4 | `DefaultRateLimiter` with the retry by version and `on-failure`; unit tests on a `Clock` | ~200 lines |
| 5 | `RateLimitState` to bytes and back, a format with a version | ~150 lines |

## Stage 2, in parallel

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 6 | `acquire(maxWait)`, `RateLimitExceededException` | 1 |
| 7 | `RateLimiterRegistry` with `findByName`, `InMemoryRateLimiterRegistry` | 1 |
| 8 | `observability/micrometer` | 1 |
| 9 | `spring/webmvc`: the filter, `KeyResolver`, `RateLimitRejectionHandler`, the headers | 1 |
| 10 | `FixedWindow` | 5 |
| 11 | `SlidingWindowLog`, accepted requests only | 5 |
| 12 | `SlidingWindowCounter` | 5 |
| 13 | `LeakyBucket` as GCRA, `delay` in the decision | 5 |
| 14 | `spring/data/redis`: the Lua script, `PX`, Testcontainers | 5 |

## Stage 3, in parallel

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 15 | `spring/web`: `RateLimitingInterceptor` for `RestClient` | 6 |
| 16 | `spring/context`: `@RateLimit`, `RateLimitBeanPostProcessor`, `RateLimitMethodInterceptor`, the key in SpEL, `@EnableRateLimiting` | 6, 7 |

## Stage 4, in sequence

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 17 | `spring-boot/autoconfigure`: `semantic.traffic` settings, limiters by name in `RateLimiterRegistry`, the filter, the interceptor, the proxy of `@RateLimit`, 429 for `RateLimitExceededException` | 9, 15, 16, ideally 10–13 |
| 18 | `spring-boot/starter`, `spring-boot/starter/redis`; removes this file | 17 |

Each pull request adds its module to `settings.gradle.kts` and brings the map in
`docs/diagrams/traffic.py` and the README in line with what it built; conflicts in these shared
files are settled on rebase.

## Decided

- The answer is `RateLimitDecision` of `allow(key, cost)`, the storage is `RateLimitRepository`, the formula is
  `RateLimitStrategy`, after the strategies of Flask-Limiter.
- `on-failure: allow` by default.
- Settings are `permit-limit` per `window`, after ASP.NET.
- `RateLimit` headers are off by default.
- `SlidingWindowLog` logs accepted requests only.
- `@RateLimit` lives in `spring/context` and is applied by a post-processor, after Spring 7
  `@ConcurrencyLimit`; no AspectJ.
- Limiters are found by name in `RateLimiterRegistry`; an unknown name stops the application at startup.
- The proxy of `@RateLimit` sits inside the one of `@Retryable`: every attempt takes a permit.
