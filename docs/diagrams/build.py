#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

The drawings follow the rules of the batcher diagrams, so the libraries read as
one family:

- Show the thing, not a box with its name; draw everything to one scale.
- Plates, not frames: a group is a flat fill with no outline.
- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting, red is a failure, grey is structure. On
  the dark canvas coloured plates are bright with dark text. In the module maps
  blue is plain JVM, green is Spring, grey is a module that is not there yet.
- No legends. Every part is labelled where it stands: a row by its level in
  the gutter, a family by its status next to its name.
- Every line is solid; a lighter colour, never a dash, marks what is inactive.
  The exception is the family map: a family in development is filled, one
  planned for v1 is dashed, one for later is dotted.
- One grid of 4 px. Space inside a group is smaller than space between groups:
  12 inside a block, 16 between blocks, 24 between plates.
- Two text sizes: the name in bold, the explanation in grey. Below the name
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
	("Spring Data", [("green", "ddd/spring-data", "semantic-ddd-spring-data", "what every store shares")]),
	(None, [("green", "ddd/spring-data-jpa", "semantic-ddd-spring-data-jpa", "aggregates as JPA entities"),
		("green", "ddd/spring-data-mongodb", "semantic-ddd-spring-data-mongodb", "aggregates as MongoDB documents")]),
	("Spring Boot", [("green", "ddd/spring-boot", "semantic-ddd-spring-boot", "auto-configuration"),
		("green", "ddd/spring-boot-starter", "semantic-ddd-spring-boot-starter", "the way in: add it and one store")]),
]


def families(pal):
	# the whole library on one page, a family per plate, the one in development on top
	a = Art(720, 0, pal)
	y = family(a, 16, "ddd", "in development, v1", [
		("JVM", [("blue", "ddd/core", "semantic-ddd", "building blocks, no framework")]),
		*DDD_SPRING,
	])
	y = family(a, y, "vo", "planned, v1", [
		("base", [(None, "vo/core", "semantic-vo", "the validation every value object shares")]),
		("groups", [(None, "vo/net", "semantic-vo-net", "email, URL, IP"),
			(None, "vo/range", "semantic-vo-range", "bounded ranges")]),
		(None, [(None, "vo/time", "semantic-vo-time", "dates, periods")]),
	], DASH)
	y = family(a, y, "cqrs", "after v1", [
		(None, [(None, "cqrs/core", "semantic-cqrs", "commands, queries, buses")]),
	], DOT)
	a.h = y - PLATES + 16
	return a


def modules(pal):
	# the ddd family alone, with the vo module it builds on: read top to bottom, each level builds on the
	# one above it
	a = Art(720, 0, pal)
	y = family(a, 16, "ddd", "in development, v1", [
		("JVM", [(None, "vo/core", "semantic-vo", "value objects, the vo family"),
			("blue", "ddd/core", "semantic-ddd", "building blocks, no framework")]),
		*DDD_SPRING,
	])
	a.h = y - PLATES + 16
	return a


DIAGRAMS = {"families": families, "modules": modules}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
