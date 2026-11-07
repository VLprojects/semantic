#!/usr/bin/env python3
"""Generates docs/assets/edd-*.svg: the planned modules of event-driven design and their links.

Drawn with the parts of build.py; to be merged into it with the other module maps.
"""

import textwrap

import build as b

EXTRA = {
	"light": {"teal-plate": "#D9F3EF", "teal-soft": "#2C6B63", "teal-ink": "#1F2328", "card-soft": "#59636E"},
	"dark": {"teal-plate": "#4FD1C5", "teal-soft": "#0B4A44", "teal-ink": "#04201D", "card-soft": "#9198A1"},
}
for theme, palette in b.PALETTE.items():
	palette.update(EXTRA[theme])

W = 430
GUT = 190
IN_GAP, FAM_GAP, BUS = 96, 150, 96
ROW_GAP = 56
FRAME = 16
LEVELS_GAP = 40
X0 = 24
C = [X0 + GUT]
for gap in (IN_GAP, FAM_GAP, BUS, IN_GAP, FAM_GAP):
	C.append(C[-1] + W + gap)
RIGHT_LANE = 72
PAGE_W = C[5] + W + RIGHT_LANE + 24

LEVELS = [
	("other families", "already in the tree; events use them", [0]),
	("JVM", "Kotlin and one library; no Spring, no Jakarta", [1, 2]),
	("Spring", "spring-context, spring-tx, spring-jdbc, spring-orm, spring-amqp, spring-messaging, Micrometer",
		[3, 4, 5, 6]),
	("Spring Data", "repositories, mapping and callbacks of Spring Data", [7, 8]),
	("Spring Boot", "@AutoConfiguration on @ConditionalOnClass; a starter only gathers dependencies", [9, 10]),
]
HUE = {"other families": "card", "JVM": "blue", "Spring": "green", "Spring Data": "teal", "Spring Boot": "amber"}

# row, columns, directory, breadcrumbs, artifact, analogue in Spring Modulith, abstractions, boundary
BLOCKS = [
	(0, [2], "architecture/boundary/core", "boundary", "semantic-boundary", None,
		["@Integrated", "Scope", "@IntroducedIn"],
		"Annotations of a contract at the system boundary, no runtime. edd takes the outside name and version of an event from them."),
	(0, [5], "architecture/pattern/resilience/core", "resilience", "semantic-resilience", None,
		["Backoff", "FixedBackoff", "ExponentialBackoff"],
		"Only the delay for an attempt number. Whoever calls it retries and schedules."),

	(1, [0, 1], "design/ddd/core", "ddd", "semantic-ddd", None,
		["Event", "EventMeta", "EventId", "AggregateRoot", "Revision"],
		"An aggregate keeps its events but does not publish them. Knows neither edd nor Spring."),
	(1, [2], "design/edd/core", "edd", "semantic-edd", "events-api",
		["EventPublication", "PublicationStatus", "EventPublicationRegistry", "EventSerializer", "EventIdResolver",
			"Externalization", "IncompletePublications", "FailedPublications", "CompletionMode"],
		"An event is any object. No Spring, transactions or stores: only the model of a publication and the SPI."),
	(1, [3], "design/edd/jackson", "edd › jackson", "semantic-edd-jackson", "events-jackson",
		["JacksonEventSerializer", "EventTypeResolver"],
		"JSON only. Writes the type of an event from @Integrated, else from its class name."),
	(2, [3], "design/edd/test", "edd › test", "semantic-edd-test", "test",
		["InMemoryEventPublicationRegistry", "PublishedEvents", "AssertablePublishedEvents"],
		"No Spring, for unit tests: a registry in memory and checks of what was published."),
	(1, [5], "architecture/pattern/delivery/core", "delivery", "semantic-delivery", None,
		["Message", "MessageSender", "OutboxStore", "OutboxStatus", "Relay", "InboxStore", "IdempotentConsumer"],
		"Delivers any message and knows nothing of events. No Spring, no broker clients."),

	(3, [2], "design/edd/spring/core", "edd › spring", "semantic-edd-spring", "events-core",
		["PersistentEventMulticaster", "@ReliableListener", "ExternalizingListener", "PublicationCompletion"],
		"Writes a publication per listener in the transaction and calls it after commit. Sends outside through MessageSender."),
	(3, [3], "design/edd/spring/jdbc", "edd › spring › jdbc", "semantic-edd-spring-jdbc", "events-jdbc",
		["JdbcEventPublicationRegistry", "event_publication", "event_publication_archive"],
		"PostgreSQL through JdbcTemplate. The migration of a service creates the schema, a flag creates it otherwise."),
	(4, [3], "design/edd/spring/jpa", "edd › spring › jpa", "semantic-edd-spring-jpa", "events-jpa",
		["JpaEventPublicationRegistry", "JpaEventPublication"],
		"Through EntityManager in the transaction of a service. No Spring Data."),
	(5, [3], "design/edd/spring/observability", "edd › spring › observability", "semantic-edd-spring-observability",
		"observability", ["ObservedEventMulticaster", "PublicationObservation", "edd.publications"],
		"Micrometer Observation: spans and counters. A service sets up the export."),
	(6, [3], "design/edd/spring/test", "edd › spring › test", "semantic-edd-spring-test", "test",
		["Scenario", "@EnableScenarios", "PublishedEventsExtension"],
		"Test scope only: JUnit 5 and spring-test. A stimulus, a wait, a check of the event."),
	(3, [5], "architecture/pattern/delivery/spring/jdbc", "delivery › spring › jdbc", "semantic-delivery-spring-jdbc",
		None, ["JdbcOutboxStore", "JdbcInboxStore", "outbox_message", "inbox_message"],
		"PostgreSQL: SKIP LOCKED with a lease in locked_until. A message is written in the transaction of a service."),
	(4, [5], "architecture/pattern/delivery/spring/rabbitmq", "delivery › spring › rabbitmq",
		"semantic-delivery-spring-rabbitmq", "events-amqp", ["RabbitMessageSender"],
		"Sends through RabbitTemplate, the key is the routing key. Sent once a publisher confirm comes."),
	(5, [5], "architecture/pattern/delivery/spring/messaging", "delivery › spring › messaging",
		"semantic-delivery-spring-messaging", "events-messaging", ["MessageChannelSender"],
		"The channel is a bean named by the route, the key goes in a header; Spring Integration takes it from there."),

	(7, [0, 1], "design/ddd/spring/data/core", "ddd › spring › data", "semantic-ddd-spring-data", None,
		["AggregateEventCollector", "EntityRepository", "AggregateRepository", "SpecificationExecutor"],
		"Collects the events of the aggregates of a transaction and hands them to ApplicationEventPublisher before commit. "
		"Knows nothing of edd."),
	(8, [0], "design/ddd/spring/data/jpa", "ddd › spring › data › jpa", "semantic-ddd-spring-data-jpa", None,
		["JpaEntityRepository", "JpaAggregateRepository", "CollectingEntityListener"],
		"Puts roots into the collector on @PostLoad and @PrePersist."),
	(8, [1], "design/ddd/spring/data/mongodb", "ddd › spring › data › mongodb", "semantic-ddd-spring-data-mongodb",
		None, ["MongoEntityRepository", "MongoAggregateRepository", "CollectingConvertCallback"],
		"Puts roots into the collector on AfterConvert and BeforeConvert."),
	(7, [3], "design/edd/spring/mongodb", "edd › spring › mongodb", "semantic-edd-spring-mongodb", "events-mongodb",
		["MongoEventPublicationRegistry", "event_publication"],
		"Through MongoTemplate; transactions need a replica set."),

	(9, [0, 1], "design/ddd/spring-boot/autoconfigure", "ddd › spring-boot", "semantic-ddd-spring-boot", None,
		["DddSpringDataAutoConfiguration"],
		"Only registers beans of the modules it finds and the collector."),
	(9, [2, 3], "design/edd/spring-boot/autoconfigure", "edd › spring-boot", "semantic-edd-spring-boot", "events-core",
		["EddAutoConfiguration", "EddProperties", "IncompletePublicationsResubmitter", "StalePublicationsMonitor"],
		"Only configuration under semantic.edd and schedules: resubmit on start, stale publications, the archive. "
		"The logic lives in edd-spring."),
	(9, [4], "design/edd/spring-boot/actuator", "edd › spring-boot › actuator", "semantic-edd-spring-boot-actuator",
		None, ["EventPublicationsEndpoint"],
		"Reads and resubmits publications through the API of the registry; stores nothing."),
	(9, [5], "architecture/pattern/delivery/spring-boot/autoconfigure", "delivery › spring-boot",
		"semantic-delivery-spring-boot", None, ["DeliveryAutoConfiguration", "DeliveryProperties", "ScheduledRelay"],
		"Configuration under semantic.delivery, Relay on a schedule, metrics of the outbox and the inbox."),
	(10, [0], "design/ddd/spring-boot/starter/jpa", "ddd › spring-boot › starter › jpa",
		"semantic-ddd-spring-boot-starter-jpa", None, [], "ddd-spring-data-jpa and auto-configuration in one dependency."),
	(10, [1], "design/ddd/spring-boot/starter/mongodb", "ddd › spring-boot › starter › mongodb",
		"semantic-ddd-spring-boot-starter-mongodb", None, [],
		"ddd-spring-data-mongodb and auto-configuration in one dependency."),
	(10, [2], "design/edd/spring-boot/starter/jdbc", "edd › spring-boot › starter › jdbc",
		"semantic-edd-spring-boot-starter-jdbc", "starter-jdbc", [],
		"edd-spring, the registry on JDBC, Jackson and auto-configuration."),
	(10, [3], "design/edd/spring-boot/starter/jpa", "edd › spring-boot › starter › jpa",
		"semantic-edd-spring-boot-starter-jpa", "starter-jpa", [],
		"edd-spring, the registry on JPA, Jackson and auto-configuration."),
	(10, [4], "design/edd/spring-boot/starter/mongodb", "edd › spring-boot › starter › mongodb",
		"semantic-edd-spring-boot-starter-mongodb", "starter-mongodb", [],
		"edd-spring, the registry on MongoDB, Jackson and auto-configuration."),
]
EXISTING = ("design/ddd/", "architecture/boundary/")

CHIP_TOP = 110
CHIP_STEP = 24
LINE = 16


def span(cols):
	return C[cols[0]], C[cols[-1]] + W - C[cols[0]]


def chip_rows(chips, w):
	rows, width = 0, None
	for chip in chips:
		cw = b.chip_w(chip)
		if width is None or width + 6 + cw > w - 24:
			rows += 1
			width = cw
		else:
			width += 6 + cw
	return rows


def wrap(text, w):
	lines, current = [], ""
	for word in text.split():
		probe = f"{current} {word}".strip()
		if b.sans_em(probe) * b.NOTE > w - 24 and current:
			lines.append(current)
			current = word
		else:
			current = probe
	return lines + [current]


def plate_height(cols, chips, boundary):
	_, w = span(cols)
	return CHIP_TOP + chip_rows(chips, w) * CHIP_STEP + 8 + len(wrap(boundary, w)) * LINE + 6


RH = {}
for row, cols, *_, chips, boundary in BLOCKS:
	RH[row] = max(RH.get(row, 0), plate_height(cols, chips, boundary))

TOP = {}
FRAMES = []
y = 24 + 56
for name, note, rows in LEVELS:
	top = y
	y += FRAME
	for i, r in enumerate(rows):
		if i:
			y += ROW_GAP
		TOP[r] = y
		y += RH[r]
	y += FRAME
	FRAMES.append((name, note, top, y - top, rows))
	y += LEVELS_GAP
PAGE_H = y - LEVELS_GAP + 24
ROW_HUE = {r: HUE[name] for name, _, _, _, rows in FRAMES for r in rows}


def mid(row):
	return TOP[row] + RH[row] / 2


def block(a, row, cols, path, crumbs, artifact, modulith, chips, boundary):
	x, w = span(cols)
	top, h, hue = TOP[row], RH[row], ROW_HUE[row]
	plate = "card" if hue == "card" else f"{hue}-plate"
	soft = "card-soft" if hue == "card" else f"{hue}-soft"
	ink = "ink" if hue == "card" else f"{hue}-ink"
	a.rect(x, top, w, h, plate, r=6)
	b.tag(a, x + 12, top + 12, path, color=soft, mono=True)
	a.t(x + 12, top + 50, crumbs, color=soft)
	a.name(x + 12, top + 72, artifact, mono=True, color=ink)
	if modulith:
		b.tag(a, x + 12, top + 82, f"Spring Modulith: {modulith}", color=soft)
	cx, cy = x + 12, top + CHIP_TOP
	for chip in chips:
		cw = b.chip_w(chip)
		if cx + cw > x + w - 12:
			cx, cy = x + 12, cy + CHIP_STEP
		b.tag(a, cx, cy, chip, color=ink, mono=True)
		cx += cw + 6
	lines = wrap(boundary, w)
	by = top + h - 10 - (len(lines) - 1) * LINE
	for i, s in enumerate(lines):
		a.t(x + 12, by + i * LINE, s, color=soft)
	if not path.startswith(EXISTING):
		a.outline(x, top, w, h, color=soft, dash="2 3")


BUILDS, DECLARES, CONFIGURES = None, "3 4", "0.01 3"


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


def up(a, x, src_row, dst_row, dash, note):
	start = TOP[src_row] - 5
	dot(a, x, start)
	_, by = head(a, x, TOP[dst_row] + RH[dst_row] + 3, 0, -1)
	line(a, f"M{x},{start} L{x},{by}", dash)
	a.t(x + 12, (TOP[src_row] + TOP[dst_row] + RH[dst_row]) / 2 + 4, note)


def elbow(a, x1, src_row, x2, dst_row, dash, note):
	start = TOP[src_row] - 5
	level = TOP[src_row] - ROW_GAP / 2
	dot(a, x1, start)
	_, by = head(a, x2, TOP[dst_row] + RH[dst_row] + 3, 0, -1)
	r = 8 if x2 > x1 else -8
	line(a, f"M{x1},{start} L{x1},{level + 8} Q{x1},{level} {x1 + r},{level} L{x2 - r},{level} "
		f"Q{x2},{level} {x2},{level - 8} L{x2},{by}", dash)
	a.t((x1 + x2) / 2, level - 8, note, anchor="middle")


def across(a, x1, x2, y, dash, note, note2):
	d = 1 if x2 > x1 else -1
	start = x1 + d * 5
	dot(a, start, y)
	bx, _ = head(a, x2 - d * 3, y, d, 0)
	line(a, f"M{start},{y} L{bx},{y}", dash)
	a.t((x1 + x2) / 2, y - 26, note, anchor="middle")
	a.t((x1 + x2) / 2, y - 10, note2, anchor="middle")


def along(a, x, y, s, color="soft"):
	half = len(s) * 3.4 + 6
	a.add(f'<g transform="rotate(-90 {x} {y})">')
	a.rect(x - half, y - 12, 2 * half, 16, "bg")
	a.t(x, y, s, anchor="middle", color=color)
	a.add("</g>")


def hop(a, x1, x2, row, dash, note):
	start = TOP[row] - 5
	level = TOP[row] - FRAME - LEVELS_GAP / 2
	dot(a, x1, start)
	_, by = head(a, x2, TOP[row] - 3, 0, 1)
	line(a, f"M{x1},{start} L{x1},{level + 8} Q{x1},{level} {x1 + 8},{level} L{x2 - 8},{level} "
		f"Q{x2},{level} {x2},{level + 8} L{x2},{by}", dash)
	a.t((x1 + x2) / 2, level - 6, note, anchor="middle")


def bus(a, lane, source_x, rows, target_x, target_y):
	ys = [mid(r) for r in rows]
	for yy in ys:
		dot(a, source_x, yy)
		line(a, f"M{source_x},{yy} L{lane},{yy}", BUILDS)
	line(a, f"M{lane},{max(ys)} L{lane},{target_y}", BUILDS)
	d = 1 if target_x > lane else -1
	bx, _ = head(a, target_x - d * 3, target_y, d, 0)
	line(a, f"M{lane},{target_y} L{bx},{target_y}", BUILDS)


def edd(pal):
	a = b.Art(PAGE_W, PAGE_H, pal)
	a.t(X0, 24 + 22, "EDD", size=16, color="ink", weight="600", mono=True)
	b.tag(a, X0 + 48, 24 + 8,
		"event-driven design: an event of any type, a registry of publications after Spring Modulith; "
		"the outbox, the inbox and the senders to a broker live in delivery")
	for name, note, top, height, rows in FRAMES:
		a.add(f'<rect x="{X0}" y="{top}" width="{PAGE_W - 2 * X0}" height="{height}" rx="12" fill="none" '
			f'stroke="{a.p["line"]}" stroke-width="1.5" stroke-linecap="round" stroke-dasharray="0.01 4"/>')
		a.t(X0 + 16, top + FRAME + 24, name, color="ink", weight="500")
		for i, s in enumerate(textwrap.wrap(note, 26)):
			a.t(X0 + 16, top + FRAME + 44 + 14 * i, s, size=11, color="mute")
	for blk in BLOCKS:
		block(a, *blk)

	third = W / 3
	up(a, C[2] + third, 1, 0, DECLARES, "api: @Integrated")
	up(a, C[5] + 2 * third, 1, 0, DECLARES, "api: Backoff")
	hop(a, C[2] + 2 * third, C[5] + third, 1, DECLARES, "api: Message and MessageSender to send outside")

	lane = C[2] + W + BUS / 2
	bus(a, lane, C[3] - 5, (1, 2), C[2] + W, mid(1))
	along(a, lane - 10, (mid(1) + mid(2)) / 2 + 10, "implement the SPI")
	bus(a, lane, C[3] - 5, (3, 4, 5, 6, 7), C[2] + W, mid(3))
	along(a, lane - 10, (mid(4) + mid(6)) / 2, "use edd-spring: registry, metrics, tests")
	up(a, C[2] + third, 3, 1, DECLARES, "api: envelope, registry, route")

	delivery_lane = C[5] - 40
	bus(a, delivery_lane, C[5] - 5, (3, 4, 5), C[5], mid(1) + 20)
	along(a, delivery_lane - 10, (mid(2) + mid(4)) / 2, "implement the SPI: store, sender")

	glane = C[1] + W + FAM_GAP / 2
	sy, ty = mid(7), mid(3)
	dot(a, C[1] + W + 5, sy, "blue")
	bx, _ = head(a, C[2] - 3, ty, 1, 0, "blue")
	line(a, f"M{C[1] + W + 5},{sy} L{glane - 8},{sy} Q{glane},{sy} {glane},{sy - 8} L{glane},{ty + 8} "
		f"Q{glane},{ty} {glane + 8},{ty} L{bx},{ty}", BUILDS, "blue")
	along(a, glane - 10, (sy + ty) / 2, "publishEvent at run time, no dependency", color="blue")

	up(a, C[0] + third, 7, 1, BUILDS, "extends AggregateRoot and Entity, collects Event")
	up(a, C[0] + third, 8, 7, BUILDS, "extends the bases; @PostLoad, @PrePersist")
	up(a, C[1] + third, 8, 7, BUILDS, "extends the bases; AfterConvertCallback")
	up(a, C[0] + third, 9, 8, CONFIGURES, "configures on @ConditionalOnClass")
	up(a, C[1] + third, 9, 8, CONFIGURES, "configures on @ConditionalOnClass")
	up(a, C[2] + third, 9, 3, CONFIGURES, "configures the registry and sending outside")
	up(a, C[3] + third, 9, 7, CONFIGURES, "configures the adapters on @ConditionalOnClass")
	across(a, C[4], C[3] + W, mid(9), DECLARES, "api:", "registry")
	for col in (0, 1, 2, 3):
		up(a, C[col] + third, 10, 9, DECLARES, "api: auto-configuration")
	elbow(a, C[4] + third, 10, C[3] + 5 * W / 6, 9, DECLARES, "api: auto-configuration")

	rl = C[5] + W + RIGHT_LANE / 2
	src = mid(9)
	dot(a, C[5] + W + 5, src)
	line(a, f"M{C[5] + W + 5},{src} L{rl},{src} L{rl},{mid(3)}", CONFIGURES)
	for r in (3, 4, 5):
		bx, _ = head(a, C[5] + W + 3, mid(r), -1, 0)
		line(a, f"M{rl},{mid(r)} L{bx},{mid(r)}", CONFIGURES)
	along(a, rl + 18, (mid(6) + mid(9)) / 2, "configures on @ConditionalOnClass")
	return a


if __name__ == "__main__":
	b.OUT.mkdir(parents=True, exist_ok=True)
	for theme, pal in b.PALETTE.items():
		(b.OUT / f"edd-{theme}.svg").write_text(edd(pal).svg())
	print("written: 2 files")
