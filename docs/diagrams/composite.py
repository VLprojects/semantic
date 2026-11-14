#!/usr/bin/env python3
"""Generates docs/assets/composite-*.svg: Composite on a tree of settings and the patterns that work with it."""

import build as b

W = 1000
L = 40
ROW, INDENT = 32, 28
TOP = 120
MONO_W = 7.8

# depth, key, node, value
TREE = [
	(0, "instances", "object", None),
	(1, "orders", "object", None),
	(2, "exponential", "object", None),
	(3, "multiplier", "leaf", "3"),
	(3, "initial-delay", "leaf", "200ms"),
	(3, "retry-on-statuses", "list", None),
	(4, "[0]", "leaf", "503"),
	(4, "[1]", "leaf", "429"),
	(1, "payments", "object", None),
	(2, "fixed", "object", None),
	(3, "delay", "leaf", "1s"),
]


def parents(rows):
	stack, result = [], []
	for i, (depth, *_rest) in enumerate(rows):
		del stack[depth:]
		result.append(stack[-1] if stack else None)
		stack.append(i)
	return result


def start(pal, title, about):
	a = b.Art(W, 0, pal)
	a.t(L, 52, title, size=16, color="ink", weight="600")
	a.t(L, 76, about, size=13, color="soft")
	return a


def finish(a, bottom):
	a.h = bottom + 40
	a.parts.insert(0, f'<rect x="16" y="16" width="{W - 32}" height="{bottom + 8}" rx="12" fill="{a.p["plate"]}"/>')
	return a


def heading(a, x, text, anchor="start"):
	a.t(x, TOP - 16, text, size=12, color="mute", anchor=anchor)


def baseline(i):
	return TOP + i * ROW + 20


def tree(a, x0, rows=TREE, path=(), dim=False):
	ups = parents(rows)
	for i, (depth, key, node, value) in enumerate(rows):
		x = x0 + depth * INDENT
		y = baseline(i)
		parent = ups[i]
		if parent is not None:
			px = x0 + rows[parent][0] * INDENT + 6
			hot = i in path and parent in path
			color = "blue" if hot else "line"
			a.path(f"M{px},{baseline(parent) + 8} L{px},{y - 9} Q{px},{y - 5} {px + 4},{y - 5} L{x - 6},{y - 5}", color, 2 if hot else 1.5)
		faded = dim and i not in path
		a.t(x, y, key, size=13, color="mute" if faded else "ink", weight="600", mono=True)
		end = x + len(key) * MONO_W + 8
		if value is None:
			a.t(end, y, "list" if node == "list" else "object", size=11, color="mute")
		else:
			a.t(end, y, "=", size=13, color="mute", mono=True)
			w = len(value) * MONO_W + 16
			a.rect(end + 16, y - 15, w, 22, "card" if faded else "blue-plate", r=4)
			a.t(end + 24, y + 1, value, size=13, color="mute" if faded else "blue-ink", weight="600", mono=True)
	return TOP + len(rows) * ROW


def composite(pal):
	a = start(pal, "Composite: a tree of settings", "Under semantic.resilience.backoff an object holds its parts by name, a list by index, a leaf holds a string")
	heading(a, L, "tree")
	bottom = tree(a, L)
	cx = 440
	heading(a, cx, "role in Composite")
	roles = {"object": ("Composite", "parts by name"), "list": ("Composite", "parts by index"), "leaf": ("Leaf", "a value, no parts")}
	for i, (_, _, node, _) in enumerate(TREE):
		role, note = roles[node]
		a.t(cx, baseline(i), role, size=13, color="ink", weight="600", mono=True)
		a.t(cx + 100, baseline(i), note, size=12, color="soft")
	a.t(L, bottom + 28, "A client calls one operation on the root, instances, and does not tell an object, a list and a leaf apart: all three are Component.", size=12, color="soft")
	return finish(a, bottom + 40)


KEYS = [
	"orders.exponential.multiplier=3",
	"orders.exponential.initial-delay=200ms",
	"orders.exponential.retry-on-statuses[0]=503",
	"orders.exponential.retry-on-statuses[1]=429",
	"payments.fixed.delay=1s",
]
CREATED = ["root", "1", "1", "1", "2", "3", "3", "4", "5", "5", "5"]


def builder(pal):
	a = start(pal, "Builder: a tree of flat keys", "The director hands the keys to the builder one by one; the builder makes the containers on the way and returns the tree")
	heading(a, L, "director: flat keys")
	for i, key in enumerate(KEYS):
		y = baseline(i)
		a.t(L, y, str(i + 1), size=12, color="mute", mono=True)
		a.t(L + 20, y, key, size=12, color="ink", mono=True)
	call_y = baseline(len(KEYS)) + 12
	a.t(L, call_y, "a step per key, then the tree", size=12, color="soft")
	tx = 480
	heading(a, tx, "builder: tree")
	bottom = tree(a, tx)
	cx = W - 40
	heading(a, cx, "made by key", anchor="end")
	for i, who in enumerate(CREATED):
		a.t(cx, baseline(i), who, size=12, color="soft" if who == "root" else "ink", anchor="end", mono=who != "root")
	a.harrow(L + 360, baseline(2) - 5, tx - 24, "blue")
	return finish(a, bottom + 8)


def orders():
	ups = parents(TREE)
	children = {i: [] for i in range(len(TREE))}
	for i, p in enumerate(ups):
		if p is not None:
			children[p].append(i)
	post, bfs = [], []

	def walk(i):
		for c in children[i]:
			walk(c)
		post.append(i)

	walk(0)
	queue = [0]
	while queue:
		i = queue.pop(0)
		bfs.append(i)
		queue += children[i]
	return post, bfs


def iterator(pal):
	a = start(pal, "Iterator: the order of a walk", "An iterator hands out the elements one by one and hides how the tree is made; a client picks the order and may stop anywhere")
	heading(a, L, "tree")
	bottom = tree(a, L)
	post, bfs = orders()
	columns = [
		("depth first, pre-order", list(range(len(TREE)))),
		("depth first, post-order", post),
		("breadth first", bfs),
	]
	for c, (title, order) in enumerate(columns):
		cx = 560 + c * 150
		heading(a, cx, title, anchor="end")
		for n, i in enumerate(order):
			a.t(cx, baseline(i), str(n + 1), size=13, color="ink", weight="600", anchor="end", mono=True)
	a.t(L, bottom + 28, "Pre-order, a container before its parts: flatten the tree to keys. Post-order, the parts before their container: count a size, check a subtree.", size=12, color="soft")
	return finish(a, bottom + 40)


def visitor(pal):
	a = start(pal, "Visitor: one operation over every element", "An element calls the method of the visitor for its class; the operation lives in the visitor, the elements do not change")
	heading(a, L, "tree")
	bottom = tree(a, L)
	mx = 440
	heading(a, mx, "element calls")
	ex = 600
	heading(a, ex, "the visitor of flat keys returns")
	calls = {"object": "visitObject", "list": "visitList", "leaf": "visitLeaf"}
	ups = parents(TREE)
	for i, (_, key, node, value) in enumerate(TREE):
		a.t(mx, baseline(i), calls[node], size=12, color="ink", mono=True)
		if value is not None:
			path, j = [], i
			while ups[j] is not None:
				path.insert(0, TREE[j][1])
				j = ups[j]
			flat = ".".join(path).replace(".[", "[")
			a.t(ex, baseline(i), f"{flat}={value}", size=12, color="ink", mono=True)
	a.t(L, bottom + 28, "A second visitor checks the tree, a third writes YAML: a class per operation over the same elements. A new element changes every visitor.", size=12, color="soft")
	return finish(a, bottom + 40)


def chain(pal):
	a = start(pal, "Chain of Responsibility: a request goes from a part to its container", "A leaf cannot read its value; each container adds its segment to the failure and passes it up")
	heading(a, L, "tree")
	route = [7, 5, 2, 1, 0]
	bottom = tree(a, L, rows=BROKEN, path=route, dim=True)
	rx = 460
	heading(a, rx, "what an element does")
	steps = {
		7: ("blue", "throws", "a failure: too-many is no status"),
		5: ("blue", "passes", "[1]"),
		2: ("blue", "passes", "retry-on-statuses[1]"),
		1: ("blue", "passes", "exponential.retry-on-statuses[1]"),
		0: ("green", "handles", "orders.exponential.retry-on-statuses[1]"),
	}
	for i, (hue, verb, what) in steps.items():
		y = baseline(i)
		a.t(rx, y, verb, size=12, color="green-text" if hue == "green" else "ink", weight="600")
		a.t(rx + 104, y, what, size=12, color="ink" if verb != "throws" else "soft", mono=verb != "throws")
	a.t(L, bottom + 28, "The root knows the prefix of its block and throws InvalidConfigurationPropertyException with the whole path, as Jackson prepends a path.", size=12, color="soft")
	return finish(a, bottom + 40)


BROKEN = [row if row[1] != "[1]" else (4, "[1]", "leaf", "too-many") for row in TREE]

SHARED_FULL = [
	(0, "instances", "object", None),
	(1, "orders", "object", None),
	(2, "fixed", "object", None),
	(3, "delay", "leaf", "1s"),
	(1, "payments", "object", None),
	(2, "fixed", "object", None),
	(3, "delay", "leaf", "1s"),
	(1, "refunds", "object", None),
	(2, "fixed", "object", None),
	(3, "delay", "leaf", "1s"),
]
SHARED_OWN = [
	(0, "instances", "object", None),
	(1, "orders", "object", None),
	(1, "payments", "object", None),
	(1, "refunds", "object", None),
]
SHARED_ONE = [
	(0, "fixed", "object", None),
	(1, "delay", "leaf", "1s"),
]


def flyweight(pal):
	a = start(pal, "Flyweight: equal subtrees are one object", "Three blocks with the same fixed.delay=1s: three copies in memory without sharing, one for all with it")
	heading(a, L, "without sharing: 10 elements")
	bottom = tree(a, L, rows=SHARED_FULL)
	rx = 520
	heading(a, rx, "with sharing: 6 elements")
	tree(a, rx, rows=SHARED_OWN)
	sx, sy = rx + 260, baseline(1) - 24
	a.rect(sx - 16, sy, 200, 2 * ROW + 24, "card", r=6)
	for i, (depth, key, node, value) in enumerate(SHARED_ONE):
		x, y = sx + depth * INDENT, sy + 28 + i * ROW
		a.t(x, y, key, size=13, color="ink", weight="600", mono=True)
		end = x + len(key) * MONO_W + 8
		if value is None:
			a.t(end, y, "object", size=11, color="mute")
		else:
			a.t(end, y, "=", size=13, color="mute", mono=True)
			a.rect(end + 16, y - 15, len(value) * MONO_W + 16, 22, "blue-plate", r=4)
			a.t(end + 24, y + 1, value, size=13, color="blue-ink", weight="600", mono=True)
	a.path(f"M{sx},{sy + 28 + 7} Q{sx + 6},{sy + 28 + 7} {sx + 6},{sy + 28 + 13} L{sx + 6},{sy + 28 + ROW - 9} Q{sx + 6},{sy + 28 + ROW - 5} {sx + 10},{sy + 28 + ROW - 5} L{sx + INDENT - 6},{sy + 28 + ROW - 5}", "line")
	for i in (1, 2, 3):
		y = baseline(i) - 5
		x = rx + INDENT + len(SHARED_OWN[i][1]) * MONO_W + 56
		a.harrow(x, y, sx - 20, "blue")
	note_y = sy + 2 * ROW + 24 + 28
	a.t(sx - 16, note_y, "a shared element knows neither", size=12, color="soft")
	a.t(sx - 16, note_y + 18, "its container nor its path", size=12, color="soft")
	a.t(L, bottom + 28, "N blocks: 1 + 3N elements against 1 + N + 2. A factory hands out a shared element by its content, as String.intern() does; it is immutable.", size=12, color="soft")
	return finish(a, bottom + 40)


DIAGRAMS = {
	"composite": composite, "composite-builder": builder, "composite-iterator": iterator,
	"composite-visitor": visitor, "composite-chain": chain, "composite-flyweight": flyweight,
}

if __name__ == "__main__":
	for name, draw in DIAGRAMS.items():
		for theme, pal in b.PALETTE.items():
			(b.OUT / f"{name}-{theme}.svg").write_text(draw(pal).svg())
