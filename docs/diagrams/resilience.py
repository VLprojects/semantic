#!/usr/bin/env python3
"""Generates docs/assets/resilience-*.svg: the patterns of resilience and the adapters of each one."""

import build as b

BLOCK_H = b.BLOCK + b.CELLS + b.CELL_H + 12
GAP = 96
ROW = 32
JVM, SPRING, ABSENT = "blue", "green", None


def cell(a, x, y, w, level, name, what):
	if level:
		a.rect(x, y, w, b.CELL_H, level + "-plate", r=4)
		a.t(x + 8, y + 17, name, size=b.PART, color=b.ink(level), weight="600", mono=True)
		a.t(x + 8, y + 31, what, size=b.TAG, color=level + "-soft")
		return
	a.outline(x, y, w, b.CELL_H, r=4, dash="2 3")
	a.t(x + 8, y + 17, name, size=b.PART, color="mute", mono=True)
	a.t(x + 8, y + 31, what, size=b.TAG, color="mute")


def pattern(a, x, y, w, directory, artifact, note, cells):
	a.rect(x, y, w, BLOCK_H, "card", r=6)
	b.tag(a, x + 12, y + 12, directory, color="soft", mono=True)
	a.name(x + 12, y + 48, artifact, mono=True)
	a.t(x + 12, y + b.BLOCK - 12, note, color="soft")
	width = (w - 24 - 3 * b.CELL_GAP) / 4
	for i, parts in enumerate(cells):
		cell(a, x + 12 + i * (width + b.CELL_GAP), y + b.BLOCK + b.CELLS, width, *parts)


def core(what):
	return JVM, "core", what


def micrometer(what):
	return JVM, "micrometer", what


def spring(what):
	return SPRING, "spring", what


def boot(what):
	return SPRING, "spring-boot", what


def none(name, why):
	return ABSENT, name, why


PATTERNS = (
	("backoff", "how long to wait, also used by delivery", (
		core("fixed, exponential"), none("micrometer", "nothing to count"),
		spring("Spring BackOff"), none("spring-boot", "no settings"))),
	("retry", "repeat a failed call", (
		core("attempts, errors"), micrometer("attempts, exhausted"),
		none("spring", "has @Retryable"), none("spring-boot", "no Spring adapter"))),
	("circuit-breaker", "stop calling a dependency that is down", (
		core("states, windows"), micrometer("state, failure rate"),
		spring("@CircuitBreaker"), boot("settings, health"))),
	("deadline", "a limit in time on what it surrounds", (
		core("virtual thread"), micrometer("exceeded"),
		spring("@Deadline"), boot("settings"))),
	("bulkhead", "a compartment of calls per dependency", (
		core("semaphore, waitLimit"), micrometer("in use, rejected"),
		none("spring", "@ConcurrencyLimit"), none("spring-boot", "no Spring adapter"))),
	("fallback", "a spare answer on failure", (
		core("spare result"), none("micrometer", "not planned"),
		none("spring", "not planned"), none("spring-boot", "not planned"))),
)


def modules(pal):
	a = b.Art(b.DEP_W, 0, pal)
	plate = b.DEP_W - 2 * b.X0
	left, width = b.X0 + b.MARGIN, plate - 2 * b.MARGIN
	w = (width - GAP) / 2
	right = left + w + GAP
	top = 16 + b.HEADER
	first = top + b.BLOCK + 48
	bottom = first + 3 * BLOCK_H + 2 * ROW
	a.rect(b.X0, 16, plate, bottom + b.MARGIN - 16, "plate", r=12)
	b.header(a, left, 16, "RESILIENCE", "a module and its own adapters per pattern; a service declares only the patterns it uses")
	b.block(a, left, top, width, "blue", "architecture/pattern/resilience/core", "semantic-resilience",
		"Policy and how policies combine; every pattern except backoff implements it, and core knows none of them")
	for i, (name, note, cells) in enumerate(PATTERNS):
		pattern(a, right if i % 2 else left, first + (i // 2) * (BLOCK_H + ROW), w,
			f"resilience/{name}", f"semantic-resilience-{name}", note, cells)
	middle = first + BLOCK_H / 2
	b.across(a, right, left + w, middle, b.DECLARES)
	a.t((left + w + right) / 2, middle - 12, "pause", anchor="middle")
	a.h = bottom + b.MARGIN
	return a


if __name__ == "__main__":
	for theme, pal in b.PALETTE.items():
		(b.OUT / f"resilience-{theme}.svg").write_text(modules(pal).svg())
