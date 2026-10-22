#!/usr/bin/env python3
"""Generates docs/assets/*.svg: one light and one dark file per diagram.

The drawings follow the rules of the batcher diagrams, so the libraries read as
one family:

- Show the thing, not a box with its name; draw everything to one scale.
- Plates, not frames: a group is a flat fill with no outline.
- Colour means the same thing everywhere. Blue is data on the move, green is
  done or allowed, amber is waiting, red is a failure, grey is structure. On
  the dark canvas coloured plates are bright with dark text. In the module map
  green is Spring, blue is plain JVM, grey is another family.
- Every line is solid; a lighter colour, never a dash, marks what is inactive.
  The exception is the family map: a family in development is filled, one
  planned for v1 is dashed, one for later is dotted.
- One grid of 4 px. Space inside a group is smaller than space between groups.
- Two text sizes: the name in bold, the explanation in grey. Below the name
  where there is room; in a compact row, on the same line, aligned right.
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

NAME, NOTE, CODE = 14, 12.5, 13
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



# ---------------------------------------------------------------------- 0
DASH, DOT = "6 4", "0.1 4"  # planned for v1, after v1


def families(pal):
	# the whole library on one page, a family per plate. The family in development is filled and
	# coloured; a planned one is only an outline, dashed for v1 and dotted for later, and its modules are
	# grey wash with no outline of their own. The gutter on the left names the level of each row
	a = Art(720, 0, pal)
	x0, cw, gap, gut = 16, 688, 8, 72
	bx, bw = x0 + gut, cw - gut - 12  # where the blocks of a row start, and how wide the row is
	a.t(16, 20, "group", color="soft")
	a.name(60, 20, "pro.vlprojects.semantic", mono=True)
	a.t(704, 20, "solid: in development, dashed: v1, dotted: after v1", color="soft", anchor="end")

	def family(y, h, name, status, dash):
		if dash:
			a.outline(x0, y, cw, h, r=12, dash=dash)
		else:
			a.rect(x0, y, cw, h, "plate", r=12)
		a.name(x0 + 16, y + 23, name, mono=True)
		pw = 7.2 * len(status) + 20
		px = x0 + cw - 12 - pw
		if dash:
			a.outline(px, y + 9, pw, 20, r=10, dash=dash)
			a.t(px + pw / 2, y + 23, status, color="soft", anchor="middle", size=12)
		else:
			a.rect(px, y + 9, pw, 20, "blue", r=10)
			a.t(px + pw / 2, y + 23, status, color="on-blue", anchor="middle", size=12, weight="600")

	def level(y, h, name):
		a.t(x0 + 16, y + h / 2 + 4, name, color="soft")

	def block(x, y, w, h, hue, prefix, title, note):
		if hue == "dot":
			a.outline(x, y, w, h, dash=DOT)
		else:
			a.rect(x, y, w, h, (hue + "-plate") if hue else "card", r=6)
		soft = (hue + "-soft") if hue and hue != "dot" else "soft"
		ink_ = ink(hue) if hue and hue != "dot" else "ink"
		if h < 60:  # compact: name on one line, the note aligned right
			a.t(x + 12, y + h / 2 + 5, prefix, color=soft, mono=True, size=12)
			a.name(x + 12 + 7.2 * len(prefix), y + h / 2 + 5, title, mono=True, color=ink_)
			a.t(x + w - 12, y + h / 2 + 5, note, color=soft, anchor="end")
		else:
			a.t(x + 12, y + 22, prefix, color=soft, mono=True, size=12)
			a.name(x + 12, y + 41, title, mono=True, color=ink_)
			a.t(x + 12, y + 64, note, color=soft)

	def row(y, h, specs):
		w = (bw - (len(specs) - 1) * gap) / len(specs)
		for c, spec in enumerate(specs):
			block(bx + c * (w + gap), y, w, h, *spec)

	# ddd: in development
	y = 36
	family(y, 36 + 80 + gap + 80 + 12, "ddd", "in development, v1", None)
	level(y + 36, 80, "JVM")
	row(y + 36, 80, [("blue", "semantic-", "ddd", "building blocks, no framework"),
		("blue", "semantic-ddd-", "jpa", "JPA mapping, no Spring")])
	level(y + 36 + 80 + gap, 80, "Spring")
	row(y + 36 + 80 + gap, 80, [("green", "semantic-ddd-", "spring-data", "repositories"),
		("green", "semantic-ddd-", "spring-boot", "auto-configuration"),
		("green", "semantic-ddd-", "spring-boot-starter", "dependencies only")])

	# vo: planned for v1, a base every group builds on
	y += 36 + 80 + gap + 80 + 12 + 12
	family(y, 36 + 44 + gap + 80 + 12, "vo", "planned, v1", DASH)
	level(y + 36, 44, "base")
	row(y + 36, 44, [(None, "semantic-", "vo", "the validation every value object shares")])
	level(y + 36 + 44 + gap, 80, "groups")
	row(y + 36 + 44 + gap, 80, [(None, "semantic-vo-", "net", "email, URL, IP"),
		(None, "semantic-vo-", "range", "bounded ranges"),
		(None, "semantic-vo-", "time", "dates, periods"),
		("dot", "semantic-vo-", "...", "more, later")])

	# cqrs: after v1
	y += 36 + 44 + gap + 80 + 12 + 12
	family(y, 36 + 44 + 12, "cqrs", "after v1", DOT)
	level(y + 36, 44, "base")
	row(y + 36, 44, [(None, "semantic-", "cqrs", "commands, queries, handlers, buses")])
	a.h = y + 36 + 44 + 12 + 16
	return a


# ---------------------------------------------------------------------- 1
def modules(pal):
	# the group is one container with two rows of blocks: plain JVM on top, Spring below; read left to
	# right, top to bottom, each module builds on the one before it. A block names its artifact in two
	# lines, the common prefix in grey and the part that tells them apart in bold
	a = Art(720, 264, pal)
	cx, cy, cw = 16, 16, 688
	gap, h = 8, 88
	w = (cw - 24 - 2 * gap) / 3
	rows = [
		[(None, "semantic-", "vo", "value objects"),
			("blue", "semantic-", "ddd", "building blocks, no framework"),
			("blue", "semantic-ddd-", "jpa", "JPA mapping, no Spring")],
		[("green", "semantic-ddd-", "spring-data", "repositories"),
			("green", "semantic-ddd-", "spring-boot", "auto-configuration"),
			("green", "semantic-ddd-", "spring-boot-starter", "dependencies only")],
	]
	y0 = cy + 36
	a.rect(cx, cy, cw, y0 + len(rows) * (h + gap) - gap + 12 - cy, "plate", r=12)  # outer radius = inner radius + padding
	a.t(cx + 16, cy + 23, "group", color="soft")
	a.name(cx + 60, cy + 23, "pro.vlprojects.semantic", mono=True)
	a.t(cx + cw - 16, cy + 23, "blue: plain JVM, green: Spring", color="soft", anchor="end")
	for r, row in enumerate(rows):
		y = y0 + r * (h + gap)
		for c, (hue, prefix, title, note) in enumerate(row):
			x = cx + 12 + c * (w + gap)
			a.rect(x, y, w, h, (hue + "-plate") if hue else "card", r=6)
			soft = (hue + "-soft") if hue else "soft"
			a.t(x + 14, y + 24, prefix, color=soft, mono=True, size=12)
			a.name(x + 14, y + 44, title, mono=True, color=ink(hue) if hue else "ink")
			a.t(x + 14, y + 72, note, color=soft)
	return a


DIAGRAMS = {"families": families, "modules": modules}

if __name__ == "__main__":
	OUT.mkdir(parents=True, exist_ok=True)
	for diagram, build in DIAGRAMS.items():
		for theme, pal in PALETTE.items():
			(OUT / f"{diagram}-{theme}.svg").write_text(build(pal).svg())
	print(f"written: {len(DIAGRAMS) * 2} files")
