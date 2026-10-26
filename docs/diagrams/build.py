#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

The drawings follow the rules of the batcher diagrams, so the libraries read as
one family. A new diagram is drawn with the parts below, plate(), block() and ghost(),
and passes the checklist of the Gorbunov and Birman bureau before it is shown;
spacing is measured on the render, not read off the code:

- Show the thing, not a box with its name; draw everything to one scale.
- Plates, not frames: a group is a flat fill with no outline.
- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting, red is a failure, grey is structure. On
  the dark canvas coloured plates are bright with dark text. In the module maps
  blue is plain JVM, green is Spring, a pale ghost in a fine dashed outline a
  module that is not there yet.
- No legends. Every part is labelled where it stands: a layer by its name and
  what it holds in the gutter, a family by what it is next to its name.
- Every line is solid; a lighter colour, never a dash, marks what is inactive.
  The exceptions are the dotted frame of a layer, the fine dashes of a ghost
  and the arrows of the dependency map, whose line is their kind.
- One grid of 4 px. Space inside a group is smaller than space between groups:
  4 between the lines of a cell, 8 around and between cells, 12 inside a block,
  16 from its note to its cells, 16 between blocks of one layer and from a layer to its
  blocks, 32 between layers, 48 between plates; on the dependency map the
  arrows take 48 between layers.
- A block is a module: a tag with its directory, its whole artifact name in
  bold 8 below, and what it is for in grey, apart from them and 12 above the
  bottom, as the tag is 12 below the top. Never a prefix over a tail. A block
  alone in its row spans the row.
- A family name outranks the artifacts in it: 16 against 13.
- Two text sizes for content: the name in bold, the explanation in grey. Below the name
  where there is room; in a compact row, on the same line, aligned right.
  A tag is a third, smaller one: a directory or a status on a tint of its own
  text colour, with no outline.
- Every part is named in plain lowercase words, types included. Only what one would type is monospaced: calls,
  annotations, artifacts, config keys. Product names keep their capitals.
- All diagrams share one scale and are embedded at their native width, except
  the family map at the top of the root README, which spans the page. Each
  sits on its own rounded surface, the colour of a GitHub code block.
- The README embeds each one twice, with #gh-light-mode-only and
  #gh-dark-mode-only: GitHub hides one by its own theme setting, while a
  <picture> media query would follow the OS instead.
"""

import math
import pathlib
import textwrap
import unicodedata

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

PALETTE = {
	"light": {
		"bg": "#F6F8FA", "ink": "#1F2328", "soft": "#59636E", "mute": "#8C959F",
		"plate": "#FFFFFF", "slot": "#DDE2E7", "line": "#AFB8C1",
		"wash": "#FFFFFF", "card": "#E3E8ED", "ghost": "#F9FAFB", "bar": "#BEC7D0",
		"blue": "#1A7CFF", "green": "#22C063", "amber": "#FF9F2E", "red": "#EE2D62",
		"sky": "#9DD1FF", "slate": "#7A8594", "lime": "#22C063",
		"blue-plate": "#E3EFFF", "blue-ink": "#1F2328", "green-plate": "#DDF7E7", "green-ink": "#1F2328", "green-glyph": "#22C063", "amber-plate": "#FFEEDC", "amber-ink": "#1F2328", "amber-glyph": "#FF9F2E",
		"green-soft": "#3A7550", "blue-soft": "#41679C", "amber-soft": "#8F4600", "green-text": "#12873F", "amber-text": "#B85300",
		"violet-link": "#8B5CF6",
		"on-green": "#0B2915", "on-blue": "#FFFFFF", "on-amber": "#2E1700", "on-sky": "#0B2340", "on-slate": "#FFFFFF", "on-lime": "#0B2915",
	},
	"dark": {
		"bg": "#151B23", "ink": "#E6EDF3", "soft": "#9198A1", "mute": "#6E7681",
		"plate": "#1D242D", "slot": "#323B46", "line": "#515B66",
		"wash": "#1D242D", "card": "#29313C", "ghost": "#29313C", "bar": "#454F5A",
		"blue": "#4493F8", "green": "#3FB950", "amber": "#FF9F4A", "red": "#FF4F7B",
		"sky": "#6E9BFF", "slate": "#545D68", "lime": "#5BF08A",
		"blue-plate": "#6E9BFF", "blue-ink": "#06173D", "green-plate": "#5BF08A", "green-ink": "#03240E", "green-glyph": "#0D7A32", "amber-plate": "#FFB46B", "amber-ink": "#1F1206", "amber-glyph": "#8A3F00",
		"green-soft": "#0D5A26", "blue-soft": "#1B3A80", "amber-soft": "#4A2808", "green-text": "#03240E", "amber-text": "#FF9F4A",
		"violet-link": "#A78BFA",
		"on-green": "#0D1117", "on-blue": "#0D1117", "on-amber": "#0D1117", "on-sky": "#06173D", "on-slate": "#E6EDF3", "on-lime": "#03240E",
	},
}

NAME, NOTE, CODE = 14, 12, 13
PAD = 12  # surface around the drawing, on top of its own 16 px margin
REC, PITCH = 6, 8  # record square and its pitch
BAR_W, BAR_H, BAR_PITCH = 6, 28, 10  # batch bar


class Art:
	def __init__(self, width, height, pal):
		self.w, self.h, self.p, self.parts = width, height, pal, []

	def add(self, markup):
		self.parts.append(markup)

	# text -------------------------------------------------------------
	def t(self, x, y, s, size=NOTE, color="soft", anchor="start", weight="400", mono=False):
		s = s.replace("<", "&lt;").replace(">", "&gt;")
		self.add(
			f'<text x="{x:.1f}" y="{y:.1f}" font-family="{MONO if mono else FONT}" font-size="{size}" '
			f'font-weight="{weight}" fill="{self.p[color]}" text-anchor="{anchor}">{s}</text>'
		)

	def name(self, x, y, s, anchor="start", color="ink", mono=False):
		self.t(x, y, s, size=CODE if mono else NAME, color=color, anchor=anchor, weight="600", mono=mono)

	def label(self, x, y, title, note=None, anchor="start", mono=False, color="ink", note_mono=False):
		"""Name with a grey note below; y is the baseline of the name."""
		self.name(x, y, title, anchor=anchor, color=color, mono=mono)
		if note:
			self.t(x, y + 18, note, anchor=anchor, mono=note_mono)

	# shapes -----------------------------------------------------------
	def rect(self, x, y, w, h, color, r=2):
		self.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" rx="{r}" fill="{self.p[color]}"/>')

	def plate(self, x, y, w, h, color="plate"):
		self.rect(x, y, w, h, color, r=6)

	def record(self, x, y, color="blue"):
		self.rect(x, y, REC, REC, color, r=1)

	def bar(self, x, y, color="blue", h=BAR_H):
		self.rect(x, y, BAR_W, h, color, r=1.5)

	def outline(self, x, y, w, h, color="line", r=6, dash="6 4"):
		width = 2 if dash.startswith("0") else 1.5  # a dot reads lighter than a dash of the same width
		self.add(
			f'<rect x="{x + 0.75:.1f}" y="{y + 0.75:.1f}" width="{w - 1.5:.1f}" height="{h - 1.5:.1f}" rx="{r}" '
			f'fill="none" stroke="{self.p[color]}" stroke-width="{width}" stroke-dasharray="{dash}" stroke-linecap="round"/>'
		)

	def path(self, d, color="line", width=1.5):
		self.add(
			f'<path d="{d}" stroke="{self.p[color]}" stroke-width="{width}" fill="none" '
			f'stroke-linecap="round" stroke-linejoin="round"/>'
		)

	def arrow(self, x1, y, x2, color="line"):
		"""Horizontal arrow with a solid head that ends at x2."""
		self.path(f"M{x1},{y} L{x2 - 6},{y}", color)
		self.add(f'<path d="M{x2 - 7},{y - 4} L{x2},{y} L{x2 - 7},{y + 4} Z" fill="{self.p[color]}"/>')

	def harrow(self, x1, y, x2, color="line", width=1.5):
		"""Horizontal arrow from x1 to x2 in either direction, the head touching x2."""
		d = 1 if x2 > x1 else -1
		self.path(f"M{x1},{y} L{x2 - d * 6},{y}", color, width)
		self.add(f'<path d="M{x2 - d * 7},{y - 4} L{x2},{y} L{x2 - d * 7},{y + 4} Z" fill="{self.p[color]}"/>')

	def varrow(self, x, y1, y2, color="line", width=1.5):
		"""Vertical arrow pointing down to y2."""
		self.path(f"M{x},{y1} L{x},{y2 - 6}", color, width)
		self.add(f'<path d="M{x - 4},{y2 - 7} L{x},{y2} L{x + 4},{y2 - 7} Z" fill="{self.p[color]}"/>')

	def svg(self):
		w, h = self.w + 2 * PAD, self.h + 2 * PAD
		body = "\n    ".join(self.parts)
		return (
			f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}" role="img">\n'
			f'  <rect width="{w}" height="{h}" rx="12" fill="{self.p["bg"]}"/>\n'
			f'  <g transform="translate({PAD},{PAD})">\n    {body}\n  </g>\n</svg>\n'
		)


def ink(hue):
	"""Title colour on a plate of this hue: dark on the bright ones."""
	return hue + "-ink" if hue in ("amber", "green", "blue") else "ink"



# ---------------------------------------------------------------------- family plates
# A family is a plate: its name and what it is head it, and its layers, JVM, Spring or Spring Boot, stand
# under it as dotted frames across the plate, each named in the gutter, as on the dependency map. A module
# that is not there yet is a ghost; a family with none of its modules there is all ghosts. Spacing climbs
# 12 < 16 < 32 < 48: text inside a block, blocks of one layer and a layer to its blocks, layers, plates
X0 = 16  # plate edge
GAP, BLOCK = 16, 92  # gap between blocks, block height
TITLE = 16  # a family outranks the artifacts in it
TAG, TAG_H = 11, 18  # text size and height of a tag
FAMILY_W, LAYERS, PLATES = 1240, 32, 48  # page, gap between layers, gap between plates
CELL_H, CELL_GAP, PART = 42, 8, 12  # a cell inside a block, the gap between cells, the size of its name
CELLS = 5  # from the bottom of the note to the cells: 16 on the render, as between blocks


# advance of each printable ASCII glyph of SF Pro, in thousandths of an em, from space to tilde
SANS = [
	205, 267, 403, 605, 605, 807, 669, 254, 323, 323, 400, 605, 215, 429, 215, 279, 605, 444, 566,
	592, 605, 585, 617, 546, 600, 617, 215, 215, 605, 605, 605, 489, 874, 636, 604, 686, 678, 552,
	527, 707, 699, 224, 495, 600, 524, 830, 699, 731, 577, 731, 599, 593, 580, 696, 631, 924, 635,
	612, 617, 323, 279, 323, 605, 527, 500, 503, 554, 501, 554, 512, 304, 549, 540, 206, 205, 485,
	205, 803, 527, 530, 549, 549, 308, 465, 302, 527, 483, 715, 468, 486, 467, 322, 215, 323, 605,
]
TRACKING = 1.15  # a browser sets small text looser than the font draws it


def sans_em(s):
	"""Width of s in the sans font, in ems."""
	return sum(SANS[ord(c) - 32] if " " <= c <= "~" else 600 for c in s) / 1000 * TRACKING


def tag(a, x, y, text, color="soft", mono=False):
	"""A tag whose top left corner is at x, y: the colour of its text at a fifth, no outline."""
	w = (len(text) * 0.6 if mono else sans_em(text)) * TAG + 16
	a.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{TAG_H}" rx="4" fill="{a.p[color]}" fill-opacity="0.2"/>')
	a.t(x + 8, y + 12.5, text, size=TAG, color=color, mono=mono)
	return w


def block(a, x, y, w, hue, directory, artifact, note, inside=()):
	# three roles, three means: where the module lives is a tag, the artifact a build declares is bold,
	# what it is for is grey. The tag and the artifact name one thing and sit 8 apart; the note is apart
	# from them and rests 12 above the bottom, as the tag hangs 12 below the top. A grey block is a module that
	# does not exist yet or belongs to another family
	a.rect(x, y, w, height(inside), (hue + "-plate") if hue else "card", r=6)
	soft = (hue + "-soft") if hue else "soft"
	tag(a, x + 12, y + 12, directory, color=soft, mono=True)
	a.name(x + 12, y + 48, artifact, mono=True, color=ink(hue) if hue else "ink")
	a.t(x + 12, y + BLOCK - 12, note, color=soft)
	cell = (w - 24 - (len(inside) - 1) * CELL_GAP) / max(len(inside), 1)
	for i, (name, what) in enumerate(inside):
		part(a, x + 12 + i * (cell + CELL_GAP), y + BLOCK + CELLS, cell, hue, name, what)


def height(inside):
	"""A block that shows what is inside it grows by a row of cells, 12 off its bottom."""
	return BLOCK + CELLS + CELL_H + 12 if inside else BLOCK


def part(a, x, y, w, hue, name, what):
	"""A package or a type of a module: a cell on a tint of the block, its name in mono and what it holds."""
	soft = hue + "-soft"
	a.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{CELL_H}" rx="4" fill="{a.p[soft]}" fill-opacity="0.12"/>')
	a.t(x + 8, y + 17, name, size=PART, color=ink(hue), weight="600", mono=True)
	a.t(x + 8, y + 31, what, size=TAG, color=soft)


TREE = [
	[("architecture", "styles of a service and the patterns it applies", [
		("boundary", "what a component publishes to other systems and how it evolves", [
			[(1, ("blue", "architecture/boundary/core", "semantic-boundary", "architectural boundaries: contract scope, version introduced"))],
		]),
		("pattern", "a module per pattern or per group of patterns", [
			[(1, ("blue", "architecture/pattern/caching/core", "semantic-caching", "cache-aside, read-through, write-through, write-behind, refresh-ahead"))],
			[(1, ("blue", "architecture/pattern/coordination/core", "semantic-coordination", "processes acting in step", [
				("saga", "compensating steps"), ("lock", "distributed lock"),
			])), (1, ("blue", "architecture/pattern/delivery/core", "semantic-delivery", "transactional outbox and inbox: exactly once together", [
				("outbox", "at least once"), ("inbox", "at most once"), ("idempotency", "idempotency keys"),
			]))],
			[(1, ("blue", "architecture/pattern/lifecycle/core", "semantic-lifecycle", "a process from start to stop", [
				("startup", "graceful startup"), ("warmup", "caches, pools, JIT"),
				("health", "liveness, readiness"), ("shutdown", "graceful shutdown"),
			]))],
			[(1, ("blue", "architecture/pattern/persistence/cqrs/core", "semantic-cqrs", "separate write and read models", [
				("command", "commands, handlers"), ("query", "queries, handlers"), ("bus", "command, query bus"),
			])), (1, ("blue", "architecture/pattern/persistence/event-sourcing/core", "semantic-event-sourcing", "state rebuilt from events", [
				("store", "append-only store"), ("snapshot", "state checkpoints"), ("projection", "read models"),
			]))],
			[(1, ("blue", "architecture/pattern/persistence/unit-of-work/core", "semantic-unit-of-work", "changes committed atomically", [
				("tracking", "new, dirty, removed"), ("commit", "one transaction"),
			])), (1, ("blue", "architecture/pattern/resilience/core", "semantic-resilience", "survive a failing dependency", [
				("circuitbreaker", "fail fast"), ("retry", "transient faults"), ("backoff", "exponential, jitter"),
				("timeout", "bounded waits"), ("bulkhead", "isolated pools"), ("fallback", "degraded answer"),
			]))],
			[(1, ("blue", "architecture/pattern/rollout/core", "semantic-rollout", "features released in code", [
				("featureflag", "on, off per user"), ("killswitch", "emergency off"), ("canary", "growing share"),
				("darklaunch", "run, not shown"), ("experiment", "A/B tests"),
			])), (1, ("blue", "architecture/pattern/traffic/core", "semantic-traffic", "control of incoming load", [
				("ratelimit", "quotas per client"), ("throttling", "rate smoothing"),
				("shedding", "load shedding"), ("backpressure", "slow the producer"),
			]))],
		]),
		("style", "a module per style; layered, onion, clean, explicit, each extending the one before", [
			[(1, ("blue", "architecture/style/clean/core", "semantic-clean", "dependencies point in", [
				("layer", "layer annotations"), ("error", "layer exceptions"),
			])), (1, ("blue", "architecture/style/explicit/core", "semantic-explicit", "onion and clean, no ports", [
				("layer", "layer annotations"), ("error", "layer exceptions"),
			]))],
			[(1, ("blue", "architecture/style/layered/core", "semantic-layered", "tiers call downward", [
				("layer", "layer annotations"), ("error", "layer exceptions"),
			])), (1, ("blue", "architecture/style/onion/core", "semantic-onion", "rings around a domain", [
				("layer", "layer annotations"), ("error", "layer exceptions"),
			]))],
		]),
	])],
	[("correctness", "what is right and what went wrong", [(None, None, [
		[(1, ("blue", "correctness/constraint/core", "semantic-constraint", "constraints of a value declared once")),
			(1, ("blue", "correctness/error/core", "semantic-error", "the model of a failure; depends on nothing", [
			("failure", "failure types"), ("result", "success or failure"),
			("raise", "raise, recover"), ("accumulate", "many failures"),
		]))],
		[(1, ("blue", "correctness/rule/core", "semantic-rule", "named business checks", [
			("dsl", "rule, checkRule"), ("composite", "all, any, not"),
		])), (1, ("blue", "correctness/validation/core", "semantic-validation", "every rule of a value checked at once", [
			("validator", "validators of a type"), ("path", "where a value failed"),
		]))],
	])])],
	[("design", "approaches to designing a model", [
		("algebra", "algebraic abstractions: the laws an operation obeys", [
			[(1, ("blue", "design/algebra/core", "semantic-algebra", "orders and structures with their laws", [
				("relation", "orders, equivalence"), ("structure", "monoids, groups"),
			]))],
		]),
		("dbc", "design by contract: what a call and a type guarantee", [
			[(1, ("blue", "design/dbc/core", "semantic-dbc", "contracts checked at run time", [
				("condition", "pre, postconditions"), ("invariant", "type invariants"),
			]))],
		]),
		("ddd", "domain-driven design with Spring Data and Spring Boot adapters", [
			[(1, ("blue", "design/ddd/core", "semantic-ddd", "building blocks of a model; no framework", [
				("tactical", "building blocks", ["base", "specification", "specification.composite"]),
				("strategic", "strategic design"),
				("dsl", "business rule DSL"), ("architecture", "domain errors", ["error"]),
				("support", "collection helpers"),
			]))],
			[(1, ("blue", "design/ddd/jackson", "semantic-ddd-jackson", "identifiers in JSON as the value they box"))],
			[(1, ("green", "design/ddd/spring-boot/autoconfigure", "semantic-ddd-spring-boot", "auto-configuration of the modules on the classpath", [
				("events", "after-commit events"), ("repository", "aggregate stores"), ("mongodb", "MongoDB setup"),
				("verification", "startup checks"), ("web", "errors as HTTP"),
			]))],
			[(1, ("green", "design/ddd/spring-boot/starter/jpa", "semantic-ddd-spring-boot-starter-jpa", "dependencies of a JPA service")),
				(1, ("green", "design/ddd/spring-boot/starter/mongodb", "semantic-ddd-spring-boot-starter-mongodb", "dependencies of a MongoDB service"))],
			[(1, ("green", "design/ddd/spring/data/core", "semantic-ddd-spring-data", "aggregates in Spring Data")),
				(1, ("green", "design/ddd/spring/data/jpa", "semantic-ddd-spring-data-jpa", "JPA mapping")),
				(1, ("green", "design/ddd/spring/data/mongodb", "semantic-ddd-spring-data-mongodb", "MongoDB mapping"))],
		]),
	])],
	[("idiom", "extensions of the Kotlin standard library", [(None, None, [
		[(1, ("blue", "idiom/format/core", "semantic-format", "values written for a reader: cases and plurals", [
			("abbreviation", "shortened text"), ("mask", "hidden characters"),
		]))],
		[(1, ("blue", "idiom/helper/core", "semantic-helper", "extensions of the standard types, a package per kind", [
			("text", "symbols and words", ["predicate", "sanitizing", "transforming"]),
			("number", "sign, safe narrowing"), ("time", "durations, periods"),
		]))],
	])])],
	[
		("math", "computations over numbers", [(None, None, [
			[(1, ("blue", "math/core", "semantic-math", "a package per computation", [
				("rounding", "rounding modes"), ("statistics", "mean, percentiles"),
			]))],
		])]),
		("type", "rich value objects", [(None, None, [
			[(1, ("blue", "type/version", "semantic-type-version", "a module per category", [
				("version", "version schemes"),
			]))],
		])]),
	],
]
LAYER_NOTES = {
	"JVM": "Kotlin and the JDK, or one library such as Jackson; no Spring, no Jakarta",
	"Spring": "Spring and Spring Data, mapping and callbacks; no Spring Boot, a service takes them by @Import",
	"Spring Boot": "@AutoConfiguration on @ConditionalOnClass, keys under semantic.ddd; a starter holds dependencies only",
}
UNIT = 128  # one width for every cell, so their columns line up down the page
CHIP_H, CHIP_GAP = 22, 6  # a subpackage inside the cell of its package, the gap between two
OVERFLOW = []


def header(a, x, y, name, about):
	"""The name of a family at the top of its plate, and what it is in a tag beside it."""
	a.t(x, y + 30, name, size=TITLE, color="ink", weight="600", mono=True)
	tag(a, x + 0.6 * TITLE * len(name) + 12, y + 15, about)


def caption(a, x, top, name, note=None):
	"""The name of a layer on the line of the tags of its first row, and what it holds under it."""
	a.t(x, top + 24.5, name, color="ink", weight="500")
	for i, s in enumerate(textwrap.wrap(note or LAYER_NOTES[name], int(ROOM / (0.55 * ABOUT)))):
		a.t(x, top + 42 + 13 * i, s, size=ABOUT, color="mute")


def fits(text, width, size, mono=False):
	if (0.6 * len(text) if mono else sans_em(text)) * size > width:
		OVERFLOW.append(text)


def chip_w(name):
	return 0.6 * TAG * len(name) + 16


def span(package):
	"""Cells a package takes: one, or as many as its subpackages need side by side."""
	children = package[2] if len(package) > 2 else []
	need = sum(chip_w(c) for c in children) + CHIP_GAP * (len(children) - 1) + 16
	return max(1, math.ceil((need + CELL_GAP) / (UNIT + CELL_GAP)))


def cell_w(units):
	return units * UNIT + (units - 1) * CELL_GAP


def lines(packages, w):
	"""Packages in lines of whole cells; when they do not fit in one, as many to a line as makes the lines even."""
	room = int((w + CELL_GAP) // (UNIT + CELL_GAP))
	total = sum(span(p) for p in packages)
	count = math.ceil(len(packages) / math.ceil(total / room))
	out, line = [], []
	for p in packages:
		if line and (len(line) == count or sum(span(q) for q in line) + span(p) > room):
			out.append(line)
			line = []
		line.append(p)
	return out + [line] if line else out


def tall(line):
	return CELL_H + CHIP_H + 10 if any(len(p) > 2 for p in line) else CELL_H


def extent(w, packages):
	"""Height of a module: its head, then lines of cells 8 apart and 12 off the bottom."""
	if not packages:
		return BLOCK
	ls = lines(packages, w - 24)
	return BLOCK + CELLS + sum(tall(line) for line in ls) + CELL_GAP * (len(ls) - 1) + 12


def module(a, x, y, w, h, hue, directory, artifact, note, packages=()):
	"""A module: what names it at the top, what it is for and its packages at the bottom, however tall the row."""
	a.rect(x, y, w, h, hue + "-plate", r=6)
	soft = hue + "-soft"
	for text, size, mono in ((directory, TAG, True), (artifact, CODE, True), (note, NOTE, False)):
		fits(text, w - 40 if text is directory else w - 24, size, mono)
	tag(a, x + 12, y + 12, directory, color=soft, mono=True)
	a.name(x + 12, y + 48, artifact, mono=True, color=ink(hue))
	low = y + h - extent(w, packages)
	a.t(x + 12, low + BLOCK - 12, note, color=soft)
	cy = low + BLOCK + CELLS
	for line in lines(packages, w - 24) if packages else ():
		cx, ch = x + 12, tall(line)
		for package in line:
			name, what, *children = package
			cw = cell_w(span(package))
			fits(name, cw - 16, PART, True)
			fits(what, cw - 16, TAG)
			a.add(f'<rect x="{cx:.1f}" y="{cy:.1f}" width="{cw:.1f}" height="{ch}" rx="4" fill="{a.p[soft]}" fill-opacity="0.12"/>')
			a.t(cx + 8, cy + 17, name, size=PART, color=ink(hue), weight="600", mono=True)
			a.t(cx + 8, cy + 31, what, size=TAG, color=soft)
			sx = cx + 8
			for child in children[0] if children else ():
				a.add(f'<rect x="{sx:.1f}" y="{cy + CELL_H:.1f}" width="{chip_w(child):.1f}" height="{CHIP_H}" rx="4" fill="{a.p[soft]}" fill-opacity="0.16"/>')
				a.t(sx + 8, cy + CELL_H + 15, child, size=TAG, color=ink(hue), mono=True)
				sx += chip_w(child) + CHIP_GAP
			cx += cw + CELL_GAP
		cy += ch + CELL_GAP


def grid(a, x, y, w, rows, stretch=0):
	"""Rows of modules across w, each as wide as its share of the row and as tall as the tallest; returns the bottom."""
	for i, row in enumerate(rows):
		room = w - GAP * (len(row) - 1)
		widths = [room * share / sum(s for s, _ in row) for share, _ in row]
		h = max(extent(width, spec[4] if len(spec) > 4 else ()) for width, (_, spec) in zip(widths, row))
		h += stretch if i == len(rows) - 1 else 0
		for width, (_, spec) in zip(widths, row):
			module(a, x, y, width, h, *spec)
			x += width + GAP
		x -= sum(widths) + GAP * len(row)
		y += h + GAP
	return y - GAP


def plate(a, x, y, w, name, about, sections, stretch=0):
	"""A root directory: a dotted frame per subdirectory, or its modules straight on the plate; returns its height."""
	edge, width = x + MARGIN, w - 2 * MARGIN
	top = y + 15 + TAG_H + LAYERS
	for i, (section, note, rows) in enumerate(sections):
		extra = stretch if i == len(sections) - 1 else 0
		if section is None:
			top = grid(a, edge, top, width, rows, extra) + LAYERS
			continue
		start = edge + LEVEL_W
		bottom = grid(a, start, top + FRAME, edge + width - FRAME - start, rows, extra) + FRAME
		layer(a, edge, top, width, bottom - top)
		caption(a, edge + FRAME, top + FRAME, section, note)
		top = bottom + LAYERS
	return top - LAYERS + MARGIN - y


def families(pal):
	# small roots share a row of plates, their last modules stretched so the plates end on one line
	a = Art(FAMILY_W, 0, pal)
	y, full = 16, FAMILY_W - 2 * X0
	for roots in TREE:
		w = (full - PLATES * (len(roots) - 1)) / len(roots)
		heights = [plate(Art(w, 0, pal), 0, 0, w, *root) for root in roots]
		h = max(heights)
		for i, (root, natural) in enumerate(zip(roots, heights)):
			x = X0 + i * (w + PLATES)
			a.rect(x, y, w, h, "plate", r=12)
			header(a, x + MARGIN, y, root[0], root[1])
			plate(a, x, y, w, *root, stretch=h - natural)
		y += h + PLATES
	a.h = y - PLATES + 16
	if OVERFLOW:
		raise SystemExit(f"text wider than its place: {sorted(set(OVERFLOW))}")
	return a


# ---------------------------------------------------------------------- ddd dependencies
# Which module builds on which, on the plate of the family and on its grid: every block keeps the edges of
# the row, a store and its starter share a column, and the starters, what a service adds, come last. An
# arrow points to what a module needs: it starts on a dot and ends on a filled head, both 3 off the blocks,
# and what it takes is said beside it. Every arrow is violet; its line is its kind, as in UML: solid builds
# on the classes of the module it points to, extending them or implementing them, dashed is an api dependency
# a module declares, dotted configures a module only when the service has it. An arrow that has a row to
# skip, from a starter up to its store, takes a lane beside the blocks, left for the left column and right
# for the right one, and enters them from the side; its note keeps 8 off the lane and more than the padding
# of a block off the block, so it reads with the lane. The page is wider than the family map, so the
# longest starter fits half a row. A layer, JVM, Spring or Spring Boot, is a dotted frame across the plate,
# 16 off its blocks, with its name in the gutter on the line of the tags of its first row, and 48 off the next one,
# so a frame holds its blocks closer than its neighbour. Rows inside a layer are as far apart as rows of two
# layers, so every arrow up is as long as another, and the note of one between layers sits in the middle of
# the gap between the frames
DEP_W, BEND = 1312, 8  # page, bends
FRAME, FRAMES = 16, 48  # padding of a layer, gap between layers
DEP_ROW = 2 * FRAME + FRAMES  # gap between the rows of a layer, the same as between layers
MORE = 200  # the column of the stores to come
LEVEL_W, LANE, RUN = 232, 88, 56  # gutter for the name and the notes of a layer, room beside the blocks, lane out of them
ABOUT = 9  # the notes of a layer, under its name
ROOM = LEVEL_W - RUN - 2 * FRAME  # the gutter up to the lane
HEADER = 15 + TAG_H + FRAMES  # the header stands off the layers as they do off each other
MARGIN = 24  # plate to its layers, more than a layer to its blocks
ARROW = "violet-link"
BUILDS, DECLARES, CONFIGURES = None, "3 4", "0.01 3"  # the dash of each kind; dots of 2, a pixel apart
HEAD, HALF, DOT, AIR = 5, 2.5, 2, 3  # length of a head and half its base, radius of a dot, air to a block


def line(a, d, dash):
	"""A violet line along d, dashed by its kind."""
	pattern = f' stroke-dasharray="{dash}"' if dash else ""
	width = 2 if dash == CONFIGURES else 1.5  # a dot as thin as the line is lost
	a.add(
		f'<path d="{d}" stroke="{a.p[ARROW]}" stroke-width="{width}" fill="none" stroke-linecap="round" '
		f'stroke-linejoin="round"{pattern}/>'
	)


def arrowhead(a, x, y, dx, dy):
	"""A filled head with its tip at x, y, pointing along dx, dy; returns the middle of its base."""
	bx, by = x - dx * HEAD, y - dy * HEAD
	a.add(
		f'<path d="M{x},{y} L{bx - dy * HALF},{by + dx * HALF} L{bx + dy * HALF},{by - dx * HALF} Z" '
		f'fill="{a.p[ARROW]}" stroke="{a.p[ARROW]}" stroke-width="0.5" stroke-linejoin="round"/>'
	)
	return bx, by


def dot(a, x, y):
	a.add(f'<circle cx="{x}" cy="{y}" r="{DOT}" fill="{a.p[ARROW]}"/>')


def link(a, x, y1, y2, dash):
	"""From a dot over the edge at y1 up to a head under the edge at y2."""
	start = y1 - AIR - DOT
	dot(a, x, start)
	_, by = arrowhead(a, x, y2 + AIR, 0, -1)
	line(a, f"M{x},{start} L{x},{by}", dash)


def across(a, x1, x2, y, dash):
	d = 1 if x2 > x1 else -1
	start = x1 + d * (AIR + DOT)
	dot(a, start, y)
	bx, _ = arrowhead(a, x2 - d * AIR, y, d, 0)
	line(a, f"M{start},{y} L{bx},{y}", dash)


def bypass(a, edge, lane, y1, y2, dash):
	"""From a dot beside the edge at y1 out to the lane, up it and back in, to a head beside the edge at y2."""
	d = 1 if lane > edge else -1
	start = edge + d * (AIR + DOT)
	dot(a, start, y1)
	bx, _ = arrowhead(a, edge + d * AIR, y2, -d, 0)
	line(a,
		f"M{start},{y1} L{lane - d * BEND},{y1} Q{lane},{y1} {lane},{y1 - BEND} "
		f"L{lane},{y2 + BEND} Q{lane},{y2} {lane - d * BEND},{y2} L{bx},{y2}", dash)


def along(a, x, y, s):
	"""A note that reads up a lane, centred on y, its baseline at x, on the plate where it crosses a frame."""
	half = len(s) * 3.3 + 4
	a.add(f'<g transform="rotate(-90 {x} {y})">')
	a.rect(x - half, y - 12, 2 * half, 16, "plate")
	a.t(x, y, s, anchor="middle")
	a.add("</g>")


def ghost(a, x, y, w, directory, artifact, note):
	"""Modules to come: a block paler than grey in a fine dashed outline, no arrows."""
	a.rect(x, y, w, BLOCK, "ghost", r=6)
	tag(a, x + 12, y + 12, directory, mono=True)
	a.name(x + 12, y + 48, artifact, mono=True)
	a.t(x + 12, y + BLOCK - 12, note)
	a.outline(x, y, w, BLOCK, dash="2 3")


def layer(a, x, y, w, h):
	"""A layer of the family: a dotted frame around its blocks."""
	a.add(
		f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="12" fill="none" stroke="{a.p["line"]}" '
		f'stroke-width="1.5" stroke-linecap="round" stroke-dasharray="0.01 4"/>'
	)


def dependencies(pal):
	a = Art(DEP_W, 0, pal)
	cw = DEP_W - 2 * X0
	edge, width = X0 + MARGIN, cw - 2 * MARGIN
	start, end = edge + LEVEL_W, edge + width - LANE
	bw = end - start
	w = (bw - MORE - 2 * GAP) / 2
	left, more, right = start, start + w + GAP, start + w + GAP + MORE + GAP
	tops, frames, y = [], [], 16 + HEADER
	for name, rows in (("JVM", 1), ("Spring", 2), ("Spring Boot", 2)):
		frame = y
		y += FRAME
		for row in range(rows):
			y += DEP_ROW if row else 0
			tops.append(y)
			y += BLOCK
		y += FRAME
		frames.append((name, frame, y - frame))
		y += FRAMES
	y -= FRAMES
	core, data, stores, boot, starters = tops
	a.rect(X0, 16, cw, y + MARGIN - 16, "plate", r=12)
	header(a, edge, 16, "DDD", "tactical and strategic patterns of domain-driven design for Kotlin and the Spring ecosystem")
	for name, top, height in frames:
		layer(a, edge, top, width, height)
		caption(a, edge + FRAME, top + FRAME, name)
	for x, top, width, hue, directory, artifact, note in (
		(left, core, w, "blue", "design/ddd/core", "semantic-ddd", "entities, aggregates, events; no framework"),
		(right, core, w, "blue", "design/ddd/jackson", "semantic-ddd-jackson", "identifiers in JSON as the value they box"),
		(left, data, bw, "green", "design/ddd/spring/data/core", "semantic-ddd-spring-data", "aggregates saved by a Spring Data repository"),
		(left, stores, w, "green", "design/ddd/spring/data/jpa", "semantic-ddd-spring-data-jpa", "base classes with JPA mapping"),
		(right, stores, w, "green", "design/ddd/spring/data/mongodb", "semantic-ddd-spring-data-mongodb",
			"base classes with MongoDB mapping"),
		(left, boot, bw, "green", "design/ddd/spring-boot/autoconfigure", "semantic-ddd-spring-boot", "registers beans of the modules it finds"),
		(left, starters, w, "green", "design/ddd/spring-boot/starter/jpa", "semantic-ddd-spring-boot-starter-jpa",
			"the one dependency a JPA service declares"),
		(right, starters, w, "green", "design/ddd/spring-boot/starter/mongodb", "semantic-ddd-spring-boot-starter-mongodb",
			"the one dependency a MongoDB service declares"),
	):
		block(a, x, top, width, hue, directory, artifact, note)
	ghost(a, more, stores, MORE, "design/ddd/spring/data/*", "other stores", "Redis, Cassandra, Neo4j")
	ghost(a, more, starters, MORE, "design/ddd/spring-boot/starter/*", "other starters", "one per store")
	# an arrow keeps a third of its column, so its note fits the column
	jpa, mongodb = left + w / 3, right + w / 3
	for x, source, target, dash in (
		(jpa, data, core, BUILDS), (jpa, stores, data, BUILDS), (mongodb, stores, data, BUILDS),
		(jpa, boot, stores, CONFIGURES), (mongodb, boot, stores, CONFIGURES),
		(jpa, starters, boot, DECLARES), (mongodb, starters, boot, DECLARES),
	):
		link(a, x, source, target + BLOCK, dash)
	lane_left, lane_right = start - RUN, end + RUN
	bypass(a, start, lane_left, starters + BLOCK / 2, stores + BLOCK / 2, DECLARES)
	bypass(a, end, lane_right, starters + BLOCK / 2, stores + BLOCK / 2, DECLARES)
	# Jackson stands beside the core it depends on, so its arrow runs across the column between them
	across(a, right, left + w, core + BLOCK / 2, DECLARES)
	a.t((left + w + right) / 2, core + BLOCK / 2 - 12, "api dependency: identifiers", anchor="middle")
	# a note stands 8 off its arrow, on its middle, and says what the module takes
	for x, source, target, note in (
		(jpa, data, core, "extends base classes, implements interfaces"),
		(jpa, stores, data, "extends Spring Data base classes"),
		(mongodb, stores, data, "extends Spring Data base classes"),
		(jpa, boot, stores, "configures on @ConditionalOnClass"),
		(mongodb, boot, stores, "configures on @ConditionalOnClass"),
		(jpa, starters, boot, "api dependency: auto-configuration"),
		(mongodb, starters, boot, "api dependency: auto-configuration"),
	):
		a.t(x + 20, (source + target + BLOCK) / 2 + 4, note)
	# the note of a lane reads up it, 8 off the lane on the side of the blocks; a glyph turned up stands 10
	# left of its baseline and 3 right of it
	for x, note in ((lane_left + 18, "api dependency: JPA base classes"),
			(lane_right - 11, "api dependency: MongoDB base classes")):
		along(a, x, (stores + starters + BLOCK) / 2, note)
	a.h = y + MARGIN
	return a


# ---------------------------------------------------------------------- format and helper examples
# What a function does, shown on a value: what goes in on the left, what comes out on the right. A grey
# chip is the value as written, a blue one a word on the way, a green one the result
CHIP, CELL, CELL_NOTE = 28, 7.8, 7.2  # chip height, width of a mono char at 13 and at 12


def cells(s):
	"""Width of s in mono chars: a mark adds nothing, a flag or an emoji takes two."""
	n = 0
	for c in s:
		o = ord(c)
		if unicodedata.combining(c) or o in (0x200D, 0xFE0F) or 0x1F3FB <= o <= 0x1F3FF:
			continue
		n += 1 if 0x1F1E6 <= o <= 0x1F1FF else 2 if o >= 0x1F000 else 1
	return n


def chip(a, x, y, s, hue=None):
	"""A value in a chip; returns its width."""
	w = cells(s) * CELL + 24
	a.rect(x, y, w, CHIP, (hue + "-plate") if hue else "card", r=6)
	a.t(x + 12, y + 18.5, s, size=CODE, color=ink(hue) if hue else "ink", weight="600", mono=True)
	return w


def chips(a, x, y, words, hue="blue"):
	"""Words side by side, 8 apart; returns where they end."""
	for word in words:
		x += chip(a, x, y, word, hue) + 8
	return x - 8


def head(a, x, y, color="line"):
	a.add(f'<path d="M{x - 7},{y - 4} L{x},{y} L{x - 7},{y + 4} Z" fill="{a.p[color]}"/>')


def cases(pal):
	# one name split into words and joined back in each case, the call under what it gives
	a = Art(0, 0, pal)
	h, gap = 52, 8
	results = [("shopping-cart-item", "toKebabCase()"), ("shopping_cart_item", "toSnakeCase()"),
		("SHOPPING_CART_ITEM", "toConstantCase()"), ("shoppingCartItem", "toCamelCase()"),
		("ShoppingCartItem", "toPascalCase()"), ("Shopping-Cart-Item", "toTrainCase()"),
		("Shopping cart item", "toSentenceCase()")]
	top = [16 + i * (h + gap) for i in range(len(results))]
	cy = 16 + (len(results) * h + (len(results) - 1) * gap) / 2
	name = "ShoppingCartItem"
	iw = cells(name) * CELL + 24
	a.rect(16, cy - h / 2, iw, h, "card", r=6)
	a.name(16 + 12, cy - h / 2 + 22, name, mono=True)
	a.t(16 + 12, cy - h / 2 + 40, "as written")
	x = 16 + iw + 8
	a.arrow(x, cy, x + 32)
	x += 40
	end = chips(a, x, cy - CHIP / 2, ["Shopping", "Cart", "Item"])
	a.t(x, cy + CHIP / 2 + 18, "toWordSequence()", mono=True)
	x1, xm = end + 8, end + 24
	ox = xm + 24
	ow = max(cells(v) for v, _ in results) * CELL + 24
	for y, (value, call) in zip(top, results):
		yc = y + h / 2
		a.path(f"M{x1},{cy} H{xm} V{yc} H{ox - 8 - 6}")
		head(a, ox - 8, yc)
		a.rect(ox, y, ow, h, "green-plate", r=6)
		a.name(ox + 12, y + 22, value, mono=True, color=ink("green"))
		a.t(ox + 12, y + 40, call, color="green-soft", mono=True)
	a.w, a.h = ox + ow + 16, top[-1] + h + 16
	return a


def words(pal):
	# where a name breaks into words, one rule a row, told on the right
	a = Art(600, 0, pal)
	rows = [
		("HTTPRequest", ["HTTP", "Request"], "an acronym stays one word"),
		("order_line.id", ["order", "line", "id"], "any separator splits"),
		("ЗаказТовара", ["Заказ", "Товара"], "any alphabet"),
		("Cart🛒Item", ["Cart", "Item"], "an emoji splits too"),
	]
	x0 = 16 + max(cells(name) for name, _, _ in rows) * CELL + 24 + 8
	for i, (name, parts, note) in enumerate(rows):
		y = 16 + i * (CHIP + 12)
		chip(a, 16, y, name)
		a.arrow(x0, y + CHIP / 2, x0 + 32)
		chips(a, x0 + 40, y, parts)
		a.t(a.w - 16, y + 18.5, note, anchor="end")
	a.h = 16 + len(rows) * (CHIP + 12) - 12 + 16
	return a


ID = "0190a3f2-7b1e-7c3a-9f4e-5d2b8a6ec41d"


def abbreviated(pal):
	# one identifier shortened by each abbreviation: what stays in bold, what the ellipsis hides in grey
	a = Art(0, 0, pal)
	rows = [("InfixAbbreviation", 3, 2, "019…1d"), ("PrefixAbbreviation", 0, 5, "…ec41d"), ("SuffixAbbreviation", 5, 0, "0190a…")]
	gx = 16 + max(len(mode) for mode, *_ in rows) * CELL_NOTE + 16
	vw = len(ID) * CELL + 24
	for i, (mode, start, end, result) in enumerate(rows):
		y = 16 + i * (CHIP + 12)
		a.t(16, y + 18.5, mode, mono=True)
		a.rect(gx, y, vw, CHIP, "card", r=6)
		for c, char in enumerate(ID):
			kept = c < start or c >= len(ID) - end
			a.t(gx + 12 + c * CELL, y + 18.5, char, size=CODE, color="ink" if kept else "mute",
				weight="600" if kept else "400", mono=True)
		x = gx + vw + 8
		a.arrow(x, y + CHIP / 2, x + 32)
		w = chip(a, x + 40, y, result, "green")
		a.w = max(a.w, x + 40 + w + 16)
	a.h = 16 + len(rows) * (CHIP + 12) - 12 + 16
	return a


def symbols(pal):
	# what one reads as a symbol over the chars that store it, and a cut that keeps every symbol whole
	a = Art(0, 0, pal)
	parts = [("C", 1), ("a", 1), ("f", 1), ("é", 2), ("🇷🇺", 4), ("👍🏽", 4)]
	gx = 16 + len("takeSymbols(5)") * CELL_NOTE + 16
	box, step = 36, 44
	y2 = 16 + box + 8
	a.t(16, 16 + box / 2 + 4.5, "symbols")
	a.t(16, y2 + 7.5, "chars")
	for i, (glyph, units) in enumerate(parts):
		bx = gx + i * step
		a.rect(bx, 16, box, box, "card", r=6)
		a.t(bx + box / 2, 16 + box / 2 + 6, glyph, size=18, color="ink", anchor="middle")
		sx = bx + (box - (units * PITCH - (PITCH - REC))) / 2
		for u in range(units):
			a.record(sx + u * PITCH, y2)
	rx = gx + len(parts) * step - 8 + 32
	for y, call, value in [(16 + box / 2 + 4.5, "symbolCount", "6"), (y2 + 7.5, "length", "13")]:
		a.t(rx, y, call, mono=True)
		a.name(rx + 112, y, value, anchor="end", mono=True)
	y3 = y2 + REC + 24
	a.t(16, y3 + 18.5, "takeSymbols(5)", mono=True)
	chip(a, gx, y3, "Café🇷🇺", "green")
	a.t(rx + 112, y3 + 18.5, "never half a symbol", anchor="end")
	a.w, a.h = rx + 112 + 16, y3 + CHIP + 16
	return a


# ---------------------------------------------------------------------- bounded context examples
# A context shown as what it holds: the green plate is the context, a package under its object belongs to it
# at any depth, a package beside it does not. A row with a type names it in bold, with a note below
INDENT, TYPE_X = 20, 280  # indent of a package per level, where the types start


def context_packages(pal):
	a = Art(720, 0, pal)
	rows = [
		(0, "pro.acme", None, None, False),
		(1, "billing", "object Billing : BoundedContext", "declares the billing context", True),
		(2, "invoice", "Invoice", "in billing", True),
		(2, "payment", None, None, True),
		(3, "card", "CardPayment", "in billing too, at any depth", True),
		(1, "billingreport", "BillingReport", "not in billing: the name only starts the same", False),
		(1, "ordering", "object Ordering : BoundedContext", "a context of its own", False),
	]
	placed, y = [], 16
	for row in rows:
		h = 52 if row[2] else 32
		placed.append((y, h, row))
		y += h
	held = [(top, h) for top, h, row in placed if row[4]]
	a.rect(16, held[0][0], a.w - 32, held[-1][0] + held[-1][1] - held[0][0], "green-plate", r=6)
	for top, h, (depth, package, kind, note, inside) in placed:
		soft, strong = ("green-soft", ink("green")) if inside else ("soft", "ink")
		a.t(28 + depth * INDENT, top + (22 if kind else 20), package, size=CODE, color=soft, mono=True)
		if kind:
			a.name(TYPE_X, top + 22, kind, color=strong, mono=True)
			a.t(TYPE_X, top + 40, note, color=soft)
	a.h = y + 16
	return a


def context_uses(pal):
	# what the object of a context answers today, a call on the left and what it gives on the right, and below,
	# in a lighter colour, what the other strategic patterns will build on it
	a = Art(0, 0, pal)
	now = [
		("Billing.name", "billing", "the class name in kebab case"),
		("Invoice::class hasBoundedContext Billing", "true", "a package at or under the object"),
		("BillingReport::class hasBoundedContext Billing", "false", "a package beside it"),
		("Billing::class", "KClass<out BoundedContext>", "a reference the compiler checks"),
	]
	later = [
		"the relations of a context map name it by type",
		"architecture tests guard its boundaries",
		"its name marks events, logs and metrics",
	]
	gx = 16 + 64
	cw = max(cells(call) for call, _, _ in now) * CELL + 24
	ox = gx + cw + 8 + 40
	ow = max(max(cells(value) * CELL, len(note) * 6.4) for _, value, note in now) + 24
	y = 16
	a.t(16, y + 30, "now")
	for call, value, note in now:
		a.rect(gx, y, cw, 52, "card", r=6)
		a.name(gx + 12, y + 30, call, mono=True)
		a.arrow(gx + cw + 8, y + 26, ox - 8)
		a.rect(ox, y, ow, 52, "green-plate", r=6)
		a.name(ox + 12, y + 22, value, color=ink("green"), mono=True)
		a.t(ox + 12, y + 40, note, color="green-soft")
		y += 52 + 16
	y += 24 - 16
	a.t(16, y + 12, "next")
	for line in later:
		a.t(gx + 12, y + 12, line, size=NAME, color="mute")
		y += 28
	a.w, a.h = ox + ow + 16, y - 28 + 16 + 16
	return a


def context_reference(pal):
	# the same mistake in a reference by name and in one by type: only the type stops it before the build ends.
	# What gets through is red on grey, what the compiler stops is the green result
	a = Art(0, 0, pal)
	rows = [
		("by name", 'upstream = "biling"', "compiles", "the typo waits for a test", None),
		("by type", "upstream = Invoice::class", "does not compile", "Invoice is not a context", "green"),
	]
	gx = 16 + 64
	cw = max(cells(code) for _, code, _, _, _ in rows) * CELL + 24
	ox = gx + cw + 8 + 40
	ow = max(len(note) * 6.4 for _, _, _, note, _ in rows) + 24
	y = 16
	for gutter, code, outcome, note, hue in rows:
		a.t(16, y + 30, gutter)
		a.rect(gx, y, cw, 52, "card", r=6)
		a.name(gx + 12, y + 30, code, mono=True)
		a.arrow(gx + cw + 8, y + 26, ox - 8)
		a.rect(ox, y, ow, 52, (hue + "-plate") if hue else "card", r=6)
		a.name(ox + 12, y + 22, outcome, color=ink(hue) if hue else "red")
		a.t(ox + 12, y + 40, note, color=(hue + "-soft") if hue else "soft")
		y += 52 + 16
	a.w, a.h = ox + ow + 16, y - 16 + 16
	return a


# ---------------------------------------------------------------------- subdomain examples
# Each context with the subdomains it serves: a subdomain is a chip, its kind a tag beside it, a context
# that names none has nothing to the right of its arrow


def subdomains(pal):
	a = Art(0, 0, pal)
	rows = [
		("object Ordering", [("Pricing", "core"), ("Catalog", "supporting")]),
		("object Billing", [("Invoicing", "supporting"), ("Taxes", "generic")]),
		("object Notifications", [("Messaging", "generic")]),
		("object Reports", []),
	]
	cw = max(cells(context) for context, _ in rows) * CELL + 24
	ox = 16 + cw + 8 + 40
	right, y = ox, 16
	for context, served in rows:
		a.rect(16, y, cw, CHIP, "card", r=6)
		a.name(28, y + 18.5, context, mono=True)
		a.arrow(16 + cw + 8, y + CHIP / 2, ox - 8)
		x = ox
		for subdomain, kind in served:
			x += chip(a, x, y, subdomain) + 8
			x += tag(a, x, y + (CHIP - TAG_H) / 2, kind) + 16
		if not served:
			a.t(ox, y + 18.5, "serves no subdomain yet", color="mute")
			x = ox + 160
		right = max(right, x)
		y += CHIP + 16
	a.w, a.h = right, y - 16 + 16
	return a

DIAGRAMS = {
	"families": families, "dependencies": dependencies,
	"format-cases": cases, "helper-words": words, "format-abbreviated": abbreviated, "helper-symbols": symbols,
	"ddd-context-packages": context_packages, "ddd-context-uses": context_uses, "ddd-context-reference": context_reference,
	"ddd-subdomains": subdomains,
}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
