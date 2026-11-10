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
| 8 | `observability/micrometer` | 1 |
| 9 | `spring/webmvc`: the filter, `KeyResolver`, `RateLimitRejectionHandler`, the headers | 1 |
| 10 | `spring/data/redis`: the Lua script, `PX`, Testcontainers | 2 |

## Stage 3

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 11 | `spring/web`: `RateLimitingInterceptor` for `RestClient` | 7 |

## Stage 4, in sequence

| PR | What it holds | Needs merged |
| --- | --- | --- |
| 12 | `spring-boot/autoconfigure`: `semantic.traffic` settings, limiters by name, the filter, the interceptor | 9, 11, ideally 3–6 |
| 13 | `spring-boot/starter`, `spring-boot/starter/redis`; removes this file | 12 |

Each pull request adds its module to `settings.gradle.kts` and brings the map in
`docs/diagrams/traffic.py` and the README in line with what it built; conflicts in these shared
files are settled on rebase.

## Decided

- The decision is `RateLimitResponse`, the storage is `RateLimitRepository`.
- `on-failure: allow` by default.
- Settings are `permit-limit` per `window`, after ASP.NET.
- `RateLimit` headers are off by default.
- `SlidingWindowLog` logs accepted requests only.
