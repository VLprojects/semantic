#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

The drawings follow the rules of the batcher diagrams, so the libraries read as
one family. A new diagram is drawn with the parts below, family() and block(),
and passes the checklist of the Gorbunov and Birman bureau before it is shown;
spacing is measured on the render, not read off the code:

- Show the thing, not a box with its name; draw everything to one scale.
- Plates, not frames: a group is a flat fill with no outline.
- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting, red is a failure, grey is structure. On
  the dark canvas coloured plates are bright with dark text. In the module maps
  blue is plain JVM, green is Spring, grey is a module that is not there yet.
- No legends. Every part is labelled where it stands: a row by its level in
  the gutter, a family by its status next to its name.
- Every line is solid; a lighter colour, never a dash, marks what is inactive.
  The exceptions are the family map, where a family in development is filled,
  one planned for v1 is dashed and one for later is dotted, and the arrows of
  the dependency map, whose line is their kind.
- One grid of 4 px. Space inside a group is smaller than space between groups:
  12 inside a block, 16 between blocks and rows of one level, 24 between
  levels, 32 between plates.
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

import pathlib
import unicodedata

OUT = pathlib.Path(__file__).resolve().parent.parent / "assets"

FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Inter, Helvetica, Arial, sans-serif"
MONO = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace"

PALETTE = {
	"light": {
		"bg": "#F6F8FA", "ink": "#1F2328", "soft": "#59636E", "mute": "#8C959F",
		"plate": "#FFFFFF", "slot": "#DDE2E7", "line": "#AFB8C1",
		"wash": "#FFFFFF", "card": "#E3E8ED", "bar": "#BEC7D0",
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
		"wash": "#1D242D", "card": "#29313C", "bar": "#454F5A",
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
# A family is a plate: filled while in development, a dashed outline while planned for v1, a dotted one
# for later. Its name and status head the plate; the gutter on the left names the level of each row.
# Spacing climbs 12 < 16 < 24 < 32: text inside a block, rows of one level, levels, plates on the page
X0, CW = 16, 688  # plate edge and width
INSET, GUT, GAP, LEVEL, BLOCK = 16, 100, 16, 24, 92  # plate padding, gutter, gap between blocks and levels, block height
PLATES = 32
BX, BW = X0 + GUT, CW - GUT - INSET  # where the blocks of a row start, and how wide a row is
COLS = 2  # the longest artifact name fills half a row; a block alone in its row fills all of it
TITLE = 16  # a family outranks the artifacts in it
TAG, TAG_H = 11, 18  # text size and height of a tag
DASH, DOT = "6 4", "0.1 4"


def tag(a, x, y, text, color="soft", mono=False):
	"""A tag whose top left corner is at x, y: the colour of its text at a fifth, no outline."""
	w = len(text) * (6.6 if mono else 5.5) + 16
	a.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{TAG_H}" rx="4" fill="{a.p[color]}" fill-opacity="0.2"/>')
	a.t(x + 8, y + 12.5, text, size=TAG, color=color, mono=mono)
	return w


def row_tops(y, rows):
	"""Where each row starts and where the last one ends: a row that opens a level stands further off."""
	tops = []
	for level, *_ in rows:
		if tops:
			y += GAP if level is None else LEVEL
		tops.append(y)
		y += BLOCK
	return tops, y


def family(a, y, name, status, rows, dash=None):
	"""Draws a family at y and returns where the next one starts."""
	tops, bottom = row_tops(y + 48, rows)
	h = bottom + INSET - y
	if dash:
		a.outline(X0, y, CW, h, r=12, dash=dash)
	else:
		a.rect(X0, y, CW, h, "plate", r=12)
	a.t(X0 + INSET, y + 30, name, size=TITLE, color="ink", weight="600", mono=True)
	tag(a, X0 + INSET + 0.6 * TITLE * len(name) + 12, y + 15, status)  # centred on the cap height of the name
	for ry, (level, specs, *cols) in zip(tops, rows):
		if level:
			a.t(X0 + INSET, ry + 48, level, color="soft")  # on the baseline of the artifacts in its row
		n = cols[0] if cols else min(COLS, len(specs))
		w = (BW - (n - 1) * GAP) / n
		for c, (hue, directory, artifact, note) in enumerate(specs):
			block(a, BX + c * (w + GAP), ry, w, hue, directory, artifact, note)
	return y + h + PLATES


def block(a, x, y, w, hue, directory, artifact, note):
	# three roles, three means: where the module lives is a tag, the artifact a build declares is bold,
	# what it is for is grey. The tag and the artifact name one thing and sit 8 apart; the note is apart
	# from them and rests 12 above the bottom, as the tag hangs 12 below the top. A grey block is a module that
	# does not exist yet or belongs to another family
	a.rect(x, y, w, BLOCK, (hue + "-plate") if hue else "card", r=6)
	soft = (hue + "-soft") if hue else "soft"
	tag(a, x + 12, y + 12, directory, color=soft, mono=True)
	a.name(x + 12, y + 48, artifact, mono=True, color=ink(hue) if hue else "ink")
	a.t(x + 12, y + BLOCK - 12, note, color=soft)


DDD_SPRING = [
	("Spring", [("green", "ddd/spring-data", "semantic-ddd-spring-data", "aggregates saved by a Spring Data repository")]),
	(None, [("green", "ddd/spring-data-jpa", "semantic-ddd-spring-data-jpa", "base classes with JPA mapping"),
		("green", "ddd/spring-data-mongodb", "semantic-ddd-spring-data-mongodb", "base classes with MongoDB mapping")]),
	("Spring Boot", [("green", "ddd/spring-boot", "semantic-ddd-spring-boot", "auto-configuration")]),
	(None, [("green", "ddd/spring-boot-starter-jpa", "semantic-ddd-spring-boot-starter-jpa", "the way in for JPA")]),
	(None, [("green", "ddd/spring-boot-starter-mongodb", "semantic-ddd-spring-boot-starter-mongodb",
		"the way in for MongoDB")]),
]


def families(pal):
	# the whole library on one page, a family per plate, the ones in development on top
	a = Art(720, 0, pal)
	y = family(a, 16, "helper", "in development, v1", [
		("JVM", [("blue", "helper", "semantic-helper", "extensions of the standard types")]),
	])
	y = family(a, y, "format", "in development, v1", [
		("JVM", [("blue", "format", "semantic-format", "cases, abbreviation, masking")]),
	])
	y = family(a, y, "ddd", "in development, v1", [
		("JVM", [("blue", "ddd/core", "semantic-ddd", "entities, aggregates, events; no framework")]),
		*DDD_SPRING,
	])
	y = family(a, y, "cqrs", "in development", [
		("JVM", [("blue", "cqrs/core", "semantic-cqrs", "commands, queries, buses")]),
	])
	y = family(a, y, "vo", "planned, v1", [
		("base", [(None, "vo/core", "semantic-vo", "the validation every value object shares")]),
		("groups", [(None, "vo/net", "semantic-vo-net", "email, URL, IP"),
			(None, "vo/range", "semantic-vo-range", "bounded ranges")]),
		(None, [(None, "vo/time", "semantic-vo-time", "dates, periods")]),
	], DASH)
	a.h = y - PLATES + 16
	return a


# ---------------------------------------------------------------------- ddd dependencies
# Which module builds on which, on the plate of the family and on its grid: every block keeps the edges of
# the row, a store and its starter share a column, and the starters, what a service adds, come last. An
# arrow points to what a module needs: it starts on a dot and ends on a filled head, both 3 off the blocks,
# and what it takes is said beside it. Every arrow is violet; its line is its kind, as in UML: solid builds
# on the classes of the module it points to, extending them or implementing them, dashed is a dependency the
# starter declares, dotted configures a module only when the service has it. An arrow that has a row to
# skip, from a starter up to its store, takes a lane beside the blocks, left for the left column and right
# for the right one, and enters them from the side; its note keeps 8 off the lane and more than the padding
# of a block off the block, so it reads with the lane. The page is wider than the family map, so the
# longest starter fits half a row. The gaps that hold arrows are 48 inside a level and 56 between levels,
# on the grid of 4
DEP_W, DEP_ROW, DEP_LEVEL, BEND = 1040, 48, 56, 8  # page, gaps with arrows, bends
LANE, RUN = 88, 56  # room beside the blocks, and how far out of it the lane runs
ARROW = "violet-link"
BUILDS, DECLARES, CONFIGURES = None, "5 4", "0.1 4"  # the dash of each kind
HEAD, HALF, DOT, AIR = 5, 2.5, 2, 3  # length of a head and half its base, radius of a dot, air to a block


def line(a, d, dash):
	"""A violet line along d, dashed by its kind."""
	pattern = f' stroke-dasharray="{dash}"' if dash else ""
	a.add(
		f'<path d="{d}" stroke="{a.p[ARROW]}" stroke-width="1.5" fill="none" stroke-linecap="round" '
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
	"""A note that reads up a lane, centred on y, its baseline at x."""
	a.add(f'<g transform="rotate(-90 {x} {y})">')
	a.t(x, y, s, anchor="middle")
	a.add("</g>")


def dependencies(pal):
	a = Art(DEP_W, 0, pal)
	cw = DEP_W - 2 * X0
	start, end = BX + LANE, X0 + cw - INSET - LANE
	bw = end - start
	w = (bw - GAP) / 2
	left, right = start, start + w + GAP
	tops, y = [], 64
	for gap in (0, DEP_LEVEL, DEP_ROW, DEP_LEVEL, DEP_ROW):
		y += gap
		tops.append(y)
		y += BLOCK
	core, data, stores, boot, starters = tops
	a.rect(X0, 16, cw, y + INSET - 16, "plate", r=12)
	a.t(X0 + INSET, 46, "ddd", size=TITLE, color="ink", weight="600", mono=True)
	tag(a, X0 + INSET + 0.6 * TITLE * 3 + 12, 31, "in development, v1")
	for level, top in (("JVM", core), ("Spring", data), ("Spring Boot", boot)):
		a.t(X0 + INSET, top + 48, level)  # on the baseline of the artifacts in its row
	for x, top, width, hue, directory, artifact, note in (
		(left, core, bw, "blue", "ddd/core", "semantic-ddd", "entities, aggregates, events; no framework"),
		(left, data, bw, "green", "ddd/spring-data", "semantic-ddd-spring-data", "aggregates saved by a Spring Data repository"),
		(left, stores, w, "green", "ddd/spring-data-jpa", "semantic-ddd-spring-data-jpa", "base classes with JPA mapping"),
		(right, stores, w, "green", "ddd/spring-data-mongodb", "semantic-ddd-spring-data-mongodb",
			"base classes with MongoDB mapping"),
		(left, boot, bw, "green", "ddd/spring-boot", "semantic-ddd-spring-boot", "registers beans of the modules it finds"),
		(left, starters, w, "green", "ddd/spring-boot-starter-jpa", "semantic-ddd-spring-boot-starter-jpa",
			"the one dependency a JPA service declares"),
		(right, starters, w, "green", "ddd/spring-boot-starter-mongodb", "semantic-ddd-spring-boot-starter-mongodb",
			"the one dependency a MongoDB service declares"),
	):
		block(a, x, top, width, hue, directory, artifact, note)
	# an arrow keeps a third of its column, so its note fits the column; the one between two full rows keeps
	# the middle of the row
	middle, jpa, mongodb = start + bw / 2, left + w / 3, right + w / 3
	for x, source, target, dash in (
		(middle, data, core, BUILDS), (jpa, stores, data, BUILDS), (mongodb, stores, data, BUILDS),
		(jpa, boot, stores, CONFIGURES), (mongodb, boot, stores, CONFIGURES),
		(jpa, starters, boot, DECLARES), (mongodb, starters, boot, DECLARES),
	):
		link(a, x, source, target + BLOCK, dash)
	lane_left, lane_right = start - RUN, end + RUN
	bypass(a, start, lane_left, starters + BLOCK / 2, stores + BLOCK / 2, DECLARES)
	bypass(a, end, lane_right, starters + BLOCK / 2, stores + BLOCK / 2, DECLARES)
	# a note stands 8 off its arrow, on the middle of the gap, and says what the module takes
	for x, gap_top, gap, note, color, anchor in (
		(middle + 8, core + BLOCK, DEP_LEVEL, "extends base classes, implements interfaces", "soft", "start"),
		(jpa + 8, data + BLOCK, DEP_ROW, "extends Spring Data base classes", "soft", "start"),
		(mongodb + 8, data + BLOCK, DEP_ROW, "extends Spring Data base classes", "soft", "start"),
		(jpa + 8, stores + BLOCK, DEP_LEVEL, "configures on @ConditionalOnClass", "soft", "start"),
		(mongodb + 8, stores + BLOCK, DEP_LEVEL, "configures on @ConditionalOnClass", "soft", "start"),
		(jpa + 8, boot + BLOCK, DEP_ROW, "api dependency: auto-configuration", "soft", "start"),
		(mongodb + 8, boot + BLOCK, DEP_ROW, "api dependency: auto-configuration", "soft", "start"),
	):
		a.t(x, gap_top + gap / 2 + 4, note, color=color, anchor=anchor)
	# the note of a lane reads up it, 8 off the lane on the side of the blocks; a glyph turned up stands 10
	# left of its baseline and 3 right of it
	for x, note in ((lane_left + 18, "api dependency: JPA base classes"),
			(lane_right - 11, "api dependency: MongoDB base classes")):
		along(a, x, (stores + starters + BLOCK) / 2, note)
	a.h = y + INSET
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


DIAGRAMS = {
	"families": families, "dependencies": dependencies,
	"format-cases": cases, "helper-words": words, "format-abbreviated": abbreviated, "helper-symbols": symbols,
}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
