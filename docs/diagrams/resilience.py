#!/usr/bin/env python3
"""Generates docs/assets/resilience*-*.svg: the modules of the family and of each pattern in it."""

import build as b
import delivery as d

OVERVIEW_W, OVERVIEW_GAP = 250, 72
PAIR, TRIO = 2, 3
HUES = {"family": "card", "JVM": "blue", "Spring": "green", "Spring Boot": "amber", "used by": "card", "not built": "card"}
POLICY = "resilience/core", ["Policy"], "What every pattern except backoff is, and how policies combine."
CORE_ROW, MICROMETER_ROW, SPRING_ROW, AUTOCONFIGURE_ROW, STARTER_ROW, ACTUATOR_ROW = 1, 2, 3, 4, 5, 6


def bypass(a, m, x1, src, lane, x2, dst, dash, note):
	start, low = m.top[src] - 5, m.top[src] - d.ROW_GAP / 2
	high = m.bottom(dst) + d.ROW_GAP / 2
	d.dot(a, x1, start)
	_, by = d.head(a, x2, m.bottom(dst) + 3, 0, -1)
	d.line(a, f"M{x1},{start} L{x1},{low + 8} Q{x1},{low} {x1 + 8},{low} L{lane - 8},{low} Q{lane},{low} {lane},{low - 8} "
		f"L{lane},{high + 8} Q{lane},{high} {lane - 8},{high} L{x2 + 8},{high} Q{x2},{high} {x2},{high - 8} L{x2},{by}", dash)
	d.along(a, lane - 10, (low + high) / 2, note)


OVERVIEW = d.Map(
	6,
	[
		("JVM", "no Spring", [0, CORE_ROW, MICROMETER_ROW]),
		("Spring", "annotations, Spring types", [SPRING_ROW]),
		("Spring Boot", "wiring only", [AUTOCONFIGURE_ROW, STARTER_ROW, ACTUATOR_ROW]),
	],
	HUES,
	[
		(0, [1, 2, 3, 4, 5], "core", ["Policy"], "The common type and how policies combine. Knows no pattern, so a service pulls only the patterns it needs."),
		(CORE_ROW, [0], "backoff/core", ["Backoff"], "How long to wait between attempts."),
		(CORE_ROW, [1], "retry/core", ["Retry"], "Repeats a failed call."),
		(CORE_ROW, [2], "circuit-breaker/core", ["CircuitBreaker"], "Stops calling a dependency that is down."),
		(CORE_ROW, [3], "deadline/core", ["Deadline"], "A limit in time on what it surrounds."),
		(CORE_ROW, [4], "bulkhead/core", ["Bulkhead"], "A compartment of concurrent calls."),
		(CORE_ROW, [5], "fallback/core", ["Fallback"], "A spare answer on failure."),
		(MICROMETER_ROW, [1], "observability/micrometer", [], "Attempts, exhausted."),
		(MICROMETER_ROW, [2], "observability/micrometer", [], "State, failure rate."),
		(MICROMETER_ROW, [3], "observability/micrometer", [], "Exceeded."),
		(MICROMETER_ROW, [4], "observability/micrometer", [], "In use, rejected."),
		(SPRING_ROW, [0], "spring/core", [], "Backoff as a Spring BackOff."),
		(SPRING_ROW, [2], "spring/core", ["@CircuitBreaker"], "The annotation and its aspect."),
		(SPRING_ROW, [3], "spring/core", ["@Deadline"], "The annotation and its aspect."),
		(AUTOCONFIGURE_ROW, [2], "spring-boot/autoconfigure", [], "Settings and beans."),
		(AUTOCONFIGURE_ROW, [3], "spring-boot/autoconfigure", [], "Settings and beans."),
		(STARTER_ROW, [2], "spring-boot/starter", [], "One dependency."),
		(STARTER_ROW, [3], "spring-boot/starter", [], "One dependency."),
		(ACTUATOR_ROW, [2], "spring-boot/actuator", [], "Health and state."),
	],
	w=OVERVIEW_W,
	gap=OVERVIEW_GAP,
)


def overview(pal):
	m = OVERVIEW
	a = b.Art(m.width, m.height, pal)
	d.header(a, "RESILIENCE", "paths under architecture/pattern/resilience; a column per pattern, each one its own modules")
	m.draw(a)
	third, c = m.w / 3, m.c
	for col in range(1, 6):
		m.up(a, c[col] + third, CORE_ROW, 0, d.BUILDS, "implements")
	m.across(a, c[1], c[0] + m.w, m.top[CORE_ROW] + m.rh[CORE_ROW] / 2, d.DECLARES, "api")
	for col in range(1, 5):
		m.up(a, c[col] + third, MICROMETER_ROW, CORE_ROW, d.DECLARES, "api")
	m.up(a, c[0] + third, SPRING_ROW, CORE_ROW, d.DECLARES, "api")
	for col in (2, 3):
		lane = c[col] + m.w + OVERVIEW_GAP / 2
		bypass(a, m, c[col] + 5 * third / 2, SPRING_ROW, lane, c[col] + 5 * third / 2, CORE_ROW, d.DECLARES, "api")
		m.up(a, c[col] + third, AUTOCONFIGURE_ROW, SPRING_ROW, d.CONFIGURES, "configures")
		m.up(a, c[col] + third, STARTER_ROW, AUTOCONFIGURE_ROW, d.DECLARES, "api")
	lane = c[2] + m.w + OVERVIEW_GAP / 2
	bypass(a, m, c[2] + 5 * third / 2, ACTUATOR_ROW, lane, c[2] + 5 * third / 2, AUTOCONFIGURE_ROW, d.DECLARES, "api")
	return a


def backoff_map():
	return d.Map(
		TRIO,
		[
			("JVM", "no Spring", [0]),
			("Spring", "Spring types", [1]),
			("used by", "other modules that wait", [2]),
		],
		HUES,
		[
			(0, [0, 1, 2], "core", ["Backoff", "FixedBackoff", "ExponentialBackoff"],
				"The delay for an attempt number. Whoever calls it waits and repeats. Not a Policy: it runs nothing."),
			(1, [0], "spring/core", [], "Backoff as a Spring BackOff."),
			(2, [1], "resilience/retry/core", ["Retry"], "Pauses between attempts."),
			(2, [2], "delivery/core", ["OutboxRetryPolicy"], "Picks the next attempt of a record."),
		],
	)


def draw_backoff(a, m):
	third, c = d.W / 3, m.c
	m.up(a, c[0] + third, 1, 0, d.DECLARES, "api")
	m.up(a, c[1] + third, 2, 0, d.DECLARES, "api: Backoff")
	m.up(a, c[2] + third, 2, 0, d.DECLARES, "api: Backoff")


def retry_map():
	return d.Map(
		PAIR,
		[
			("family", "what it builds on", [0]),
			("JVM", "no Spring", [1, 2]),
			("not built", "and why", [3]),
		],
		HUES,
		[
			(0, [0], *POLICY),
			(0, [1], "resilience/backoff/core", ["Backoff"], "The pause between attempts."),
			(1, [0, 1], "core", ["Retry"], "Repeats a failed call. Stops when the attempts run out or the thread is interrupted."),
			(2, [0], "observability/micrometer", [], "Attempts and exhausted retries."),
			(3, [1], "spring/core", ["@Retryable"], "Spring Framework 7 has it."),
		],
	)


def draw_retry(a, m):
	third, c = d.W / 3, m.c
	m.up(a, c[0] + third, 1, 0, d.BUILDS, "implements Policy")
	m.up(a, c[1] + third, 1, 0, d.DECLARES, "api: Backoff")
	m.up(a, c[0] + third, 2, 1, d.DECLARES, "api")


def spring_boot_map(core, annotation, spring_note, micrometer, actuator=None):
	boot = [(4, [0], "spring-boot/autoconfigure", [], "Settings and beans."),
		(5, [0], "spring-boot/starter", [], "One dependency: the pattern and auto-configuration.")]
	if actuator:
		boot.append((4, [1], "spring-boot/actuator", [], actuator))
	return d.Map(
		PAIR,
		[
			("family", "what it builds on", [0]),
			("JVM", "no Spring", [1, 2]),
			("Spring", "annotations", [3]),
			("Spring Boot", "wiring only", [4, 5]),
		],
		HUES,
		[
			(0, [0], *POLICY),
			(1, [0, 1], "core", *core),
			(2, [1], "observability/micrometer", [], micrometer),
			(3, [0], "spring/core", [annotation], spring_note),
			*boot,
		],
	)


def draw_spring_boot(a, m, actuator=False):
	third, c = d.W / 3, m.c
	m.up(a, c[0] + third, 1, 0, d.BUILDS, "implements Policy")
	m.up(a, c[1] + third, 2, 1, d.DECLARES, "api")
	m.up(a, c[0] + third, 3, 1, d.DECLARES, "api")
	m.up(a, c[0] + third, 4, 3, d.CONFIGURES, "configures")
	m.up(a, c[0] + third, 5, 4, d.DECLARES, "api")
	if actuator:
		m.across(a, c[1], c[0] + d.W, m.top[4] + m.rh[4] / 2, d.DECLARES, "api")


def breaker_map():
	return spring_boot_map((["CircuitBreaker"], "Stops calling a dependency that is down. Closed, open, half-open over a window of calls."),
		"@CircuitBreaker", "The annotation and its aspect.", "State and failure rate.", "State in the health endpoint.")


def deadline_map():
	return spring_boot_map((["Deadline"], "A limit in time on what it surrounds: one attempt inside retry, the whole operation outside. A virtual thread and an interrupt."),
		"@Deadline", "The annotation and its aspect.", "Calls past the limit.")


def bulkhead_map():
	return d.Map(
		PAIR,
		[
			("family", "what it builds on", [0]),
			("JVM", "no Spring", [1, 2]),
			("not built", "and why", [3]),
		],
		HUES,
		[
			(0, [0], *POLICY),
			(1, [0, 1], "core", ["Bulkhead"], "A semaphore per dependency. When full it rejects at once, or waits up to waitLimit."),
			(2, [0], "observability/micrometer", [], "Places in use and rejected calls."),
			(3, [1], "spring/core", ["@ConcurrencyLimit"], "Spring Framework 7 has it."),
		],
	)


def draw_bulkhead(a, m):
	third, c = d.W / 3, m.c
	m.up(a, c[0] + third, 1, 0, d.BUILDS, "implements Policy")
	m.up(a, c[0] + third, 2, 1, d.DECLARES, "api")


def fallback_map():
	return d.Map(
		PAIR,
		[
			("family", "what it builds on", [0]),
			("JVM", "no Spring", [1]),
		],
		HUES,
		[
			(0, [0], *POLICY),
			(1, [0, 1], "core", ["Fallback"], "A spare answer on failure. Adapters are added when a service needs them."),
		],
	)


def draw_fallback(a, m):
	m.up(a, m.c[0] + d.W / 3, 1, 0, d.BUILDS, "implements Policy")


PATTERNS = (
	("backoff", backoff_map, draw_backoff, "how long to wait between attempts"),
	("retry", retry_map, draw_retry, "repeats a failed call"),
	("circuit-breaker", breaker_map, lambda a, m: draw_spring_boot(a, m, actuator=True), "stops calling a dependency that is down"),
	("deadline", deadline_map, draw_spring_boot, "a limit in time on what it surrounds"),
	("bulkhead", bulkhead_map, draw_bulkhead, "a compartment of concurrent calls per dependency"),
	("fallback", fallback_map, draw_fallback, "a spare answer on failure"),
)


def pattern(pal, name, build, draw, about):
	m = build()
	a = b.Art(m.width, m.height, pal)
	d.header(a, name.upper(), f"{about}; paths under architecture/pattern/resilience/{name}")
	m.draw(a)
	draw(a, m)
	return a


if __name__ == "__main__":
	for theme, pal in b.PALETTE.items():
		(b.OUT / f"resilience-{theme}.svg").write_text(overview(pal).svg())
		for name, build, draw, about in PATTERNS:
			(b.OUT / f"resilience-{name}-{theme}.svg").write_text(pattern(pal, name, build, draw, about).svg())
