#!/usr/bin/env python3
"""Generates docs/assets/delivery-*.svg: the modules of delivery, its neighbours and the path of a record."""

import textwrap

import build as b

W = 380
GUT = 190
GAP = 96
ROW_GAP = 56
FRAME = 16
LEVELS_GAP = 40
X0 = 24
CHIP_TOP = 40
CHIP_STEP = 24
LINE = 16
BUILDS, DECLARES, CONFIGURES = None, "3 4", "0.01 3"


def wrap(text, width, size=b.NOTE):
	lines, current = [], ""
	for word in text.split():
		probe = f"{current} {word}".strip()
		if b.sans_em(probe) * size > width and current:
			lines.append(current)
			current = word
		else:
			current = probe
	return lines + [current]


def line(a, d, dash, color=b.ARROW):
	pattern = f' stroke-dasharray="{dash}"' if dash else ""
	width = 2 if dash == CONFIGURES else 1.5
	a.add(f'<path d="{d}" stroke="{a.p[color]}" stroke-width="{width}" fill="none" stroke-linecap="round" '
		f'stroke-linejoin="round"{pattern}/>')


def head(a, x, y, dx, dy, color=b.ARROW):
	bx, by = x - dx * 5, y - dy * 5
	a.add(f'<path d="M{x},{y} L{bx - dy * 2.5},{by + dx * 2.5} L{bx + dy * 2.5},{by - dx * 2.5} Z" '
		f'fill="{a.p[color]}" stroke="{a.p[color]}" stroke-width="0.5" stroke-linejoin="round"/>')
	return bx, by


def dot(a, x, y, color=b.ARROW):
	a.add(f'<circle cx="{x}" cy="{y}" r="2" fill="{a.p[color]}"/>')


def frame(a, width, name, note, top, height):
	a.add(f'<rect x="{X0}" y="{top}" width="{width - 2 * X0}" height="{height}" rx="12" fill="none" '
		f'stroke="{a.p["line"]}" stroke-width="1.5" stroke-linecap="round" stroke-dasharray="0.01 4"/>')
	a.t(X0 + 16, top + FRAME + 24, name, color="ink", weight="500")
	for i, s in enumerate(textwrap.wrap(note, 26)):
		a.t(X0 + 16, top + FRAME + 44 + 14 * i, s, size=11, color="mute")


def header(a, name, about):
	a.t(X0, 24 + 22, name, size=16, color="ink", weight="600", mono=True)
	b.tag(a, X0 + len(name) * 9.6 + 12, 24 + 8, about)


class Map:
	def __init__(self, columns, levels, hues, blocks, lane=24, w=W, gap=GAP):
		self.w = w
		self.c = [X0 + GUT + i * (w + gap) for i in range(columns)]
		self.levels, self.blocks = levels, blocks
		self.width = self.c[-1] + w + lane + 24
		heights = {}
		for row, cols, _, chips, boundary, *_ in blocks:
			heights[row] = max(heights.get(row, 0), self.plate_height(cols, chips, boundary))
		self.rh = heights
		self.top, self.frames, self.hue = {}, [], {}
		y = 24 + 56
		for name, note, rows in levels:
			start = y
			y += FRAME
			for i, r in enumerate(rows):
				y += ROW_GAP if i else 0
				self.top[r] = y
				self.hue[r] = hues[name]
				y += heights[r]
			y += FRAME
			self.frames.append((name, note, start, y - start))
			y += LEVELS_GAP
		self.height = y - LEVELS_GAP + 24

	def span(self, cols):
		return self.c[cols[0]], self.c[cols[-1]] + self.w - self.c[cols[0]]

	def chip_rows(self, chips, w):
		rows, width = 0, None
		for chip in chips:
			cw = b.chip_w(chip)
			if width is None or width + 6 + cw > w - 24:
				rows += 1
				width = cw
			else:
				width += 6 + cw
		return rows

	def plate_height(self, cols, chips, boundary):
		_, w = self.span(cols)
		return CHIP_TOP + self.chip_rows(chips, w) * CHIP_STEP + 8 + len(wrap(boundary, w - 24)) * LINE + 6

	def bottom(self, row):
		return self.top[row] + self.rh[row]

	def block(self, a, row, cols, name, chips, boundary, later=False):
		x, w = self.span(cols)
		top, h, hue = self.top[row], self.rh[row], self.hue[row]
		plate = "card" if hue == "card" else f"{hue}-plate"
		soft = "soft" if hue == "card" else f"{hue}-soft"
		ink = "ink" if hue == "card" else f"{hue}-ink"
		a.rect(x, top, w, h, plate, r=6)
		a.name(x + 12, top + 26, name, mono=True, color=ink)
		cx, cy = x + 12, top + CHIP_TOP
		for chip in chips:
			cw = b.chip_w(chip)
			if cx + cw > x + w - 12:
				cx, cy = x + 12, cy + CHIP_STEP
			b.tag(a, cx, cy, chip, color=ink, mono=True)
			cx += cw + 6
		lines = wrap(boundary, w - 24)
		by = top + h - 10 - (len(lines) - 1) * LINE
		for i, s in enumerate(lines):
			a.t(x + 12, by + i * LINE, s, color=soft)
		if later:
			a.outline(x, top, w, h, color=soft, dash="2 3")

	def draw(self, a):
		for name, note, top, height in self.frames:
			frame(a, self.width, name, note, top, height)
		for blk in self.blocks:
			self.block(a, *blk)

	def up(self, a, x, src, dst, dash, note):
		start = self.top[src] - 5
		dot(a, x, start)
		_, by = head(a, x, self.bottom(dst) + 3, 0, -1)
		line(a, f"M{x},{start} L{x},{by}", dash)
		a.t(x + 12, (self.top[src] + self.bottom(dst)) / 2 + 4, note)

	def bend(self, a, x1, src, x2, dst, dash, note):
		start = self.top[src] - 5
		level = self.top[src] - ROW_GAP / 2
		dot(a, x1, start)
		_, by = head(a, x2, self.bottom(dst) + 3, 0, -1)
		r = 8 if x2 > x1 else -8
		line(a, f"M{x1},{start} L{x1},{level + 8} Q{x1},{level} {x1 + r},{level} L{x2 - r},{level} "
			f"Q{x2},{level} {x2},{level - 8} L{x2},{by}", dash)
		a.t(x2 + 12, self.bottom(dst) + 18, note)

	def across(self, a, x1, x2, y, dash, note):
		d = 1 if x2 > x1 else -1
		start = x1 + d * 5
		dot(a, start, y)
		bx, _ = head(a, x2 - d * 3, y, d, 0)
		line(a, f"M{start},{y} L{bx},{y}", dash)
		a.t((x1 + x2) / 2, y - 10, note, anchor="middle")


def along(a, x, y, s):
	half = len(s) * 3.4 + 6
	a.add(f'<g transform="rotate(-90 {x} {y})">')
	a.rect(x - half, y - 12, 2 * half, 16, "bg")
	a.t(x, y, s, anchor="middle")
	a.add("</g>")


MODULES = Map(
	3,
	[
		("other families", "one class from them", [0]),
		("JVM", "no Spring", [1, 2]),
		("Spring", "transactions, adapters", [3, 4]),
		("Spring Boot", "wiring only", [5, 6]),
		("outside the release", "not published or not yet", [7]),
	],
	{"other families": "card", "JVM": "blue", "Spring": "green", "Spring Boot": "amber", "outside the release": "card"},
	[
		(0, [0], "resilience/core", ["Backoff"], "The delay for an attempt number."),
		(1, [0, 1, 2], "core",
			["Outbox", "OutboxHandler", "OutboxFallbackHandler", "OutboxRetryPolicy", "OutboxRouting", "OutboxPayloadSerializer",
				"OutboxContextProvider", "OutboxInstrumentation", "OutboxMetricNames", "OutboxRecordRepository", "OutboxRecordAdministration", "Relay",
				"InboxRepository", "IdempotentReceiver", "TransactionRunner"],
			"The model, the SPI and the processing. A payload is opaque. Instrumentation does nothing by default."),
		(2, [0], "jackson", ["JacksonOutboxPayloadSerializer"], "A payload as JSON."),
		(2, [1], "observability/micrometer", ["MicrometerOutboxInstrumentation", "OutboxObservationDocumentation", "OutboxObservationConvention"],
			"Turns the events of the core into timers and spans. Optional."),
		(2, [2], "test", ["OutboxRecordRepositoryContract", "InboxRepositoryContract"], "Every storage adapter runs this contract."),
		(3, [0, 1], "spring/core", ["OutboxHandlerBeanPostProcessor", "SpringTransactionRunner", "DeliveryStartupCheck"],
			"Finds handlers, joins the transaction, fails on start when a handler, a route or a table is missing."),
		(3, [2], "spring/rabbitmq", ["RabbitOutboxHandler", "RabbitOutboxRouting"], "One more handler: done on a publisher confirm."),
		(4, [0, 1], "spring/jdbc, spring/data/jpa, spring/data/mongodb", ["*OutboxRecordRepository", "*InboxRepository"],
			"Three adapters of storage. SKIP LOCKED with a lease; findAndModify on MongoDB."),
		(5, [0, 1], "spring-boot/autoconfigure", ["DeliveryAutoConfiguration", "DeliveryProperties", "RelayScheduler"],
			"semantic.delivery settings, the lifecycle of Relay on a schedule."),
		(5, [2], "spring-boot/actuator", ["DeliveryEndpoint", "DeliveryHealthIndicator"], "Counts, pause and resume, retry of FAILED, delete."),
		(6, [0, 1], "spring-boot/starter/{jdbc, jpa, mongodb}", [], "One dependency per storage: adapter, Jackson, observability, auto-configuration."),
		(7, [0], "performance-test", [], "Throughput of the lease and of order by key."),
		(7, [1], "bom", [], "One version for the family.", True),
		(7, [2], "spring/{kafka, sns}", [], "One more handler per broker.", True),
	],
)

NAMASTACK = [
	("namastack-outbox-api, -core", "core, spring/core", "blue", "The part without Spring needs no separate API"),
	("namastack-outbox-jackson", "jackson", "blue", ""),
	("namastack-outbox-observability{, -api}", "observability/micrometer", "blue", "The contract is in core, a module per implementation"),
	("namastack-outbox-integration-tests", "test", "blue", "A contract every adapter runs"),
	("namastack-outbox-jdbc, -jpa, -mongodb", "spring/jdbc, spring/data/{jpa, mongodb}", "green", ""),
	("namastack-outbox-rabbit", "spring/rabbitmq", "green", ""),
	("namastack-outbox-starter-jdbc, -jpa, -mongodb", "spring-boot/starter/*", "amber", ""),
	("namastack-outbox-actuator", "spring-boot/actuator", "amber", "Adds pause, resume and retry of FAILED"),
	("namastack-outbox-performance-test", "performance-test", "card", ""),
	("namastack-outbox-metrics, -tracing", None, "card", "Deprecated there, nothing to carry over"),
	("partitions, instance registry", None, "card", "SKIP LOCKED with a lease replaces them"),
	(None, "core: Inbox, IdempotentReceiver", "blue", "Namastack has no inbox"),
]


def modules(pal):
	m = MODULES
	a = b.Art(m.width, m.height, pal)
	header(a, "DELIVERY", "paths under architecture/pattern/delivery; the core has no framework, the rest plugs into its SPI")
	m.draw(a)
	third, c = W / 3, m.c
	lane_left, lane_right = c[0] + W + GAP / 2, c[1] + W + GAP / 2
	m.up(a, c[0] + third, 1, 0, DECLARES, "api")
	for col, note in ((0, "implements serializer"), (1, "implements instrumentation"), (2, "api: the SPI under test")):
		m.up(a, c[col] + third, 2, 1, BUILDS if col < 2 else DECLARES, note)
	start, end = m.top[3] - 5, m.bottom(1) + 3
	dot(a, lane_left, start)
	_, by = head(a, lane_left, end, 0, -1)
	line(a, f"M{lane_left},{start} L{lane_left},{by}", DECLARES)
	along(a, lane_left - 10, (m.top[2] + m.bottom(2)) / 2, "api")
	m.bend(a, c[2] + third, 3, lane_right, 1, BUILDS, "")
	along(a, lane_right - 10, (m.top[2] + m.bottom(2)) / 2, "implements handler")
	m.up(a, c[0] + third, 4, 3, DECLARES, "api")
	m.up(a, c[0] + third, 5, 4, CONFIGURES, "configures")
	m.bend(a, c[1] + 5 * W / 6, 5, c[2] + third, 3, CONFIGURES, "configures")
	y = m.top[5] + m.rh[5] / 2
	m.across(a, c[2], c[1] + W, y, DECLARES, "api")
	m.up(a, c[0] + third, 6, 5, DECLARES, "api")
	return a


NW, NH, NGAP, NSTEP, NOTE_X = 400, 36, 72, 10, 16


def side(a, x, y, hue, name):
	if name is None:
		a.outline(x, y, NW, NH, color="mute", dash="2 3")
		return
	a.rect(x, y, NW, NH, "card" if hue == "card" else f"{hue}-plate", r=6)
	a.name(x + 12, y + 23, name, mono=True, color="ink" if hue == "card" else f"{hue}-ink")


def namastack(pal):
	left, right = X0, X0 + NW + NGAP
	width = right + NW + NOTE_X + 440 + X0
	height = 24 + 56 + len(NAMASTACK) * (NH + NSTEP) - NSTEP + 24
	a = b.Art(width, height, pal)
	header(a, "DELIVERY AND NAMASTACK", "left: Namastack, right: here; a note only where the two differ")
	for i, (theirs, ours, hue, note) in enumerate(NAMASTACK):
		y = 24 + 56 + i * (NH + NSTEP)
		mid = y + NH / 2
		side(a, left, y, "card", theirs)
		side(a, right, y, hue, ours)
		if theirs and ours:
			dot(a, left + NW + 5, mid)
			bx, _ = head(a, right - 3, mid, 1, 0)
			line(a, f"M{left + NW + 5},{mid} L{bx},{mid}", BUILDS)
		if note:
			a.t(right + NW + NOTE_X, mid + 4, note, color="soft")
	return a


CW, CH, CGAP = 252, 76, 56
FX = [X0 + GUT + i * (CW + CGAP) for i in range(4)]
FW = FX[3] + CW + 40
HEADER, FG = 24 + 56, 72


def card(a, x, y, hue, name, note, h=CH, w=CW):
	soft = "soft" if hue == "card" else f"{hue}-soft"
	ink = "ink" if hue == "card" else f"{hue}-ink"
	a.rect(x, y, w, h, "card" if hue == "card" else f"{hue}-plate", r=6)
	a.name(x + 12, y + 24, name, mono=True, color=ink)
	for i, s in enumerate(wrap(note, w - 24)):
		a.t(x + 12, y + 44 + i * LINE, s, color=soft)


def link(a, x1, x2, y):
	dot(a, x1 + 5, y)
	bx, _ = head(a, x2 - 3, y, 1, 0)
	line(a, f"M{x1 + 5},{y} L{bx},{y}", BUILDS)


def turn(a, x1, y1, x2, y2, level, note):
	dot(a, x1, y1 + 5)
	_, by = head(a, x2, y2 - 3, 0, 1)
	r = -8 if x2 < x1 else 8
	line(a, f"M{x1},{y1 + 5} L{x1},{level - 8} Q{x1},{level} {x1 + r},{level} L{x2 - r},{level} "
		f"Q{x2},{level} {x2},{level + 8} L{x2},{by}", BUILDS)
	a.t((x1 + x2) / 2, level - 8, note, anchor="middle")


def row(a, y, cards):
	for i, (hue, name, note) in enumerate(cards):
		card(a, FX[i], y, hue, name, note)
		if i:
			link(a, FX[i - 1] + CW, FX[i], y + CH / 2)


OUTCOME, OGAP = 52, 12


def flow(pal):
	top_a = HEADER
	inner_a = CH + 2 * FRAME
	top_b = top_a + inner_a + FG
	stack = 3 * OUTCOME + 2 * OGAP
	inner_b = stack + 2 * FRAME
	top_c = top_b + inner_b + FG
	a = b.Art(FW, top_c + inner_a + 24, pal)
	header(a, "THE PATH OF A RECORD", "two transactions with a relay and a channel between them")
	frame(a, FW, "writing", "one transaction of the service", top_a, inner_a)
	frame(a, FW, "relaying", "after commit, on any instance", top_b, inner_b)
	frame(a, FW, "receiving", "one transaction of the consumer", top_c, inner_a)

	ya = top_a + FRAME
	row(a, ya, (
		("blue", "Outbox.schedule", "payload and key; outside a transaction it throws"),
		("blue", "repository.add", "a record per handler, NEW, with the context"),
		("green", "commit", "the change and the record exist together"),
	))

	yb = top_b + FRAME
	mid_b = yb + stack / 2 - CH / 2
	row(a, mid_b, (
		("blue", "Relay.relay", "claims a batch: SKIP LOCKED, a lease, a token"),
		("blue", "OutboxHandler.handle", "your code or RabbitOutboxHandler"),
	))
	wide = FX[3] + CW - FX[2]
	lane = FX[1] + CW + CGAP / 2
	cy = mid_b + CH / 2
	dot(a, FX[1] + CW + 5, cy)
	line(a, f"M{FX[1] + CW + 5},{cy} L{lane},{cy}", BUILDS)
	for i, (hue, name, note) in enumerate((
		("green", "complete", "COMPLETED; a stale token changes nothing"),
		("amber", "reschedule", "OutboxRetryPolicy with Backoff picks the next attempt"),
		("red", "OutboxFallbackHandler, else FAILED", "attempts are spent"),
	)):
		y = yb + i * (OUTCOME + OGAP)
		ink = "red" if hue == "red" else f"{hue}-ink"
		a.rect(FX[2], y, wide, OUTCOME, "card" if hue == "red" else f"{hue}-plate", r=6)
		a.name(FX[2] + 12, y + 21, name, mono=True, color=ink)
		a.t(FX[2] + 12, y + 40, note, color="soft" if hue == "red" else f"{hue}-soft")
		yy = y + OUTCOME / 2
		line(a, f"M{lane},{cy} L{lane},{yy}", BUILDS)
		bx, _ = head(a, FX[2] - 3, yy, 1, 0)
		line(a, f"M{lane},{yy} L{bx},{yy}", BUILDS)

	yc = top_c + FRAME
	row(a, yc, (
		("blue", "IdempotentReceiver", "opens the transaction through TransactionRunner"),
		("blue", "Inbox.accept", "source, idempotency key, consumer; a duplicate stops here"),
		("card", "@TransactionalInbox", "the handler; what it sends goes through Outbox.schedule"),
		("green", "commit, then ack", "a crash before ack brings a duplicate"),
	))

	turn(a, FX[2] + CW / 2, ya + CH, FX[0] + CW / 2, mid_b, top_b - FG / 2, "commit makes the record visible to Relay")
	turn(a, FX[1] + CW / 2, mid_b + CH, FX[0] + CW / 2, yc, top_c - FG / 2, "at least once")
	return a


if __name__ == "__main__":
	b.OUT.mkdir(parents=True, exist_ok=True)
	for theme, pal in b.PALETTE.items():
		for name, draw in (("delivery", modules), ("delivery-namastack", namastack), ("delivery-flow", flow)):
			(b.OUT / f"{name}-{theme}.svg").write_text(draw(pal).svg())
	print("written: 6 files")
