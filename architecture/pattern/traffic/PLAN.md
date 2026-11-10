# Plan of traffic

A working file: the last pull request of the plan removes it together with `traffic-plan-*.svg`.
The design is in [README.md](README.md).

![Pull requests in sequence and in parallel](../../../docs/assets/traffic-plan-light.svg#gh-light-mode-only)
![Pull requests in sequence and in parallel](../../../docs/assets/traffic-plan-dark.svg#gh-dark-mode-only)

## Stage 1, in sequence

| PR | What it holds | Size |
| --- | --- | --- |
| 1 | `RateLimiter`, `DefaultRateLimiter` with the retry by version and `on-failure`, `RateLimitResponse`, `RateLimitCounter`, `RateLimitRepository`, `RateLimitOptimisticLockException`, `InMemoryRateLimitRepository`, `TokenBucket`; unit tests on a `Clock` | ~350 lines |
| 2 | `RateLimitState` to bytes and back, a format with a version | ~150 lines |

## Stage 2, in parallel

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 3 | `FixedWindow` | 2 |
| 4 | `SlidingWindowLog`, accepted requests only | 2 |
| 5 | `SlidingWindowCounter` | 2 |
| 6 | `LeakyBucket` as GCRA, `delay` in the response | 2 |
| 7 | `acquire(maxWait)`, `RateLimitExceededException` | 1 |
| 8 | `RateLimiterRegistry` with `findByName`, `InMemoryRateLimiterRegistry` | 1 |
| 9 | `observability/micrometer` | 1 |
| 10 | `spring/webmvc`: the filter, `KeyResolver`, `RateLimitRejectionHandler`, the headers | 1 |
| 11 | `spring/data/redis`: the Lua script, `PX`, Testcontainers | 2 |

## Stage 3, in parallel

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 12 | `spring/web`: `RateLimitingInterceptor` for `RestClient` | 7 |
| 13 | `spring/context`: `@RateLimit`, `RateLimitBeanPostProcessor`, `RateLimitMethodInterceptor`, the key in SpEL, `@EnableRateLimiting` | 7, 8 |

## Stage 4, in sequence

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 14 | `spring-boot/autoconfigure`: `semantic.traffic` settings, limiters by name in `RateLimiterRegistry`, the filter, the interceptor, the proxy of `@RateLimit`, 429 for `RateLimitExceededException` | 10, 12, 13, ideally 3–6 |
| 15 | `spring-boot/starter`, `spring-boot/starter/redis`; removes this file | 14 |

Each pull request adds its module to `settings.gradle.kts` and brings the map in
`docs/diagrams/traffic.py` and the README in line with what it built; conflicts in these shared
files are settled on rebase.

## Decided

- The decision is `RateLimitResponse`, the storage is `RateLimitRepository`.
- `on-failure: allow` by default.
- Settings are `permit-limit` per `window`, after ASP.NET.
- `RateLimit` headers are off by default.
- `SlidingWindowLog` logs accepted requests only.
- `@RateLimit` lives in `spring/context` and is applied by a post-processor, after Spring 7
  `@ConcurrencyLimit`; no AspectJ.
- Limiters are found by name in `RateLimiterRegistry`; an unknown name stops the application at startup.
- The proxy of `@RateLimit` sits inside the one of `@Retryable`: every attempt takes a permit.
