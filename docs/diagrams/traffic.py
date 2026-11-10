#!/usr/bin/env python3
"""Generates docs/assets/traffic-*.svg: the modules of traffic, its abstractions, algorithms and failures."""

import build as b
import delivery as d

W = 1000
L, R = 40, 960
CAPTION, AXIS = 11, 11


def fits(text, width, size):
	return len(text) * size * 0.56 <= width


def mono_w(text, size):
	return len(text) * size * 0.6


def start(pal, title, about):
	a = b.Art(W, 0, pal)
	a.t(L, 52, title, size=16, color="ink", weight="600")
	a.t(L, 76, about, size=13, color="soft")
	return a


def finish(a, bottom):
	a.h = bottom + 40
	a.parts.insert(0, f'<rect x="16" y="16" width="{W - 32}" height="{bottom + 8}" rx="12" fill="{a.p["plate"]}"/>')
	return a


def card(a, x, y, w, hue, status, name, note):
	assert fits(note, w - 24, b.NOTE), note
	b.block(a, x, y, w, hue, status, name, note)


def level(a, y, name, artifact):
	a.t(L, y + 12 + 12.5, name, size=13, color="ink", weight="500")
	a.t(L, y + 12 + 12.5 + 20, artifact, size=CAPTION, color="mute", mono=True)


def down(a, x, y_high, y_low, label, mono=True, dash=b.DECLARES):
	start_y = y_high + b.AIR + b.DOT
	b.dot(a, x, start_y)
	_, by = b.arrowhead(a, x, y_low - b.AIR, 0, 1)
	b.line(a, f"M{x},{start_y} L{x},{by}", dash)
	a.t(x + 20, (y_high + y_low) / 2 + 4, label, size=12, mono=mono)


def leftward(a, x_from, x_to, y, label, dash, mono=True):
	b.across(a, x_from, x_to, y, dash)
	a.t((x_from + x_to) / 2, y - 12, label, anchor="middle", size=12, mono=mono)


# ---------------------------------------------------------------- 0. duties
def duties(pal):
	a = start(pal, "One task, one abstraction", "On the left what has to be decided, on the right who decides it and nothing else")
	ox = 620
	rows = [
		("let a call through or not", "return a decision for a key and tokens", "blue", "RateLimiter"),
		("how to count calls", "a pure function: state and time in, a decision out", "blue", "RateLimitStrategy"),
		("where to keep the counter", "find by key, save with a version check", "blue", "RateLimitRepository"),
		("lose no update in a race", "on a version conflict, read again and count again", "blue", "DefaultRateLimiter"),
		("wait for a permit before an outgoing call", "wait no longer than maxWait, otherwise throw", "blue", "RateLimiter.acquire"),
		("find a limiter by its name", "one limiter for a filter, an interceptor and an annotation", "blue", "RateLimiterRegistry"),
		("limit a method of a bean", "a proxy takes a permit before the call, the key from SpEL", "green", "@RateLimit"),
		("whose request it is", "return a key for a request: client, tenant, IP", "green", "KeyResolver"),
		("how to reject", "429, Retry-After, RateLimit headers", "green", "RateLimitRejectionHandler"),
		("which limits and where", "limits by name from application.yaml", "amber", "TrafficProperties"),
	]
	y = 112
	for task, detail, hue, owner in rows:
		a.t(L, y + 18, task, size=13, color="ink", weight="600")
		a.t(L, y + 36, detail)
		w = mono_w(owner, 12) + 24
		a.rect(ox, y + 4, w, 32, hue + "-plate", r=6)
		a.t(ox + 12, y + 24.5, owner, size=12, color=b.ink(hue), weight="600", mono=True)
		y += 40 + 16
	return finish(a, y - 16)


# ---------------------------------------------------------------- 1. abstractions
BX, BW, LANE = 248, 292, 128
RX = BX + BW + LANE
ROW = b.BLOCK + 48


def abstractions(pal):
	a = start(pal, "Abstractions", "Top to bottom is the path of a call: from an HTTP request to the counter in storage")
	y = [112 + i * ROW for i in range(5)]
	level(a, y[0], "Spring", "semantic-traffic-spring-*")
	level(a, y[1], "JVM", "semantic-traffic")
	level(a, y[4], "Spring", "spring/data/redis, spring/jdbc")
	card(a, BX, y[0], BW, "green", "spring/webmvc", "RateLimitingFilter", "incoming: 429 on rejection")
	card(a, RX, y[0], BW, "green", "spring/web", "RateLimitingInterceptor", "outgoing: waits for a permit")
	card(a, BX, y[1], BW, "blue", "interface", "RateLimiter", "allow, acquire(maxWait)")
	card(a, RX, y[1], BW, "blue", "class", "DefaultRateLimiter", "reads, counts, saves by version")
	card(a, BX, y[2], BW, "blue", "sealed interface", "RateLimitStrategy", "the five algorithms of Alex Xu")
	card(a, RX, y[2], BW, "blue", "interface", "RateLimitRepository", "findByKey, save by version")
	card(a, BX, y[3], BW, "blue", "data", "RateLimitDecision", "outcome, tokens, retryAfter")
	card(a, RX, y[3], BW, "blue", "class", "InMemoryRateLimitRepository", "the default, in memory")
	card(a, RX, y[4], BW, "green", "adapters", "RedisRateLimitRepository", "shared by all instances")
	mid = b.BLOCK / 2
	down(a, BX + 48, y[0] + b.BLOCK, y[1], "allow()")
	b.line(a, f"M{RX + 48},{y[0] + b.BLOCK + b.AIR + b.DOT} L{RX + 48},{y[0] + b.BLOCK + 24} L{BX + BW - 40},{y[0] + b.BLOCK + 24} L{BX + BW - 40},{y[1] - b.AIR - b.HEAD}", b.DECLARES)
	b.dot(a, RX + 48, y[0] + b.BLOCK + b.AIR + b.DOT)
	b.arrowhead(a, BX + BW - 40, y[1] - b.AIR, 0, 1)
	a.t(RX + 64, y[0] + b.BLOCK + 20, "acquire(maxWait)", size=12, mono=True)
	leftward(a, RX, BX + BW, y[1] + mid, "implements", b.BUILDS, mono=False)
	b.line(a, f"M{RX + 48},{y[1] + b.BLOCK + b.AIR + b.DOT} L{RX + 48},{y[1] + b.BLOCK + 24} L{BX + 200},{y[1] + b.BLOCK + 24} L{BX + 200},{y[2] - b.AIR - b.HEAD}", b.DECLARES)
	b.dot(a, RX + 48, y[1] + b.BLOCK + b.AIR + b.DOT)
	b.arrowhead(a, BX + 200, y[2] - b.AIR, 0, 1)
	a.t(BX + 212, y[1] + b.BLOCK + 40, "allow(state, tokens, now)", size=12, mono=True)
	down(a, RX + 220, y[1] + b.BLOCK, y[2], "findByKey · save")
	down(a, BX + 48, y[2] + b.BLOCK, y[3], "returns", mono=False)
	b.across(a, RX + BW / 2, RX + BW / 2, 0, b.BUILDS) if False else None
	for row in (3, 4):
		edge = RX + BW
		b.bypass(a, edge, edge + 28, y[row] + mid, y[2] + mid, b.BUILDS)
	b.along(a, R - 8, (y[2] + y[4]) / 2 + mid, "implement")
	return finish(a, y[4] + b.BLOCK)


# ---------------------------------------------------------------- 2. flow
def msg(a, xs, i, j, y, label, mono=True, color="line"):
	x1, x2 = xs[i], xs[j]
	d = 1 if x2 > x1 else -1
	x1, x2 = x1 + d * 6, x2 - d * 6
	a.path(f"M{x1},{y} L{x2 - d * 6},{y}", color=color, width=1.5)
	a.add(f'<path d="M{x2},{y} L{x2 - d * 7},{y - 4} L{x2 - d * 7},{y + 4} Z" fill="{a.p[color]}"/>')
	if label:
		w = len(label) * CAPTION * (0.6 if mono else 0.56) + 8
		a.rect((x1 + x2) / 2 - w / 2, y - 21, w, 16, "plate", r=3)
		a.t((x1 + x2) / 2, y - 8, label, size=CAPTION, color="soft", anchor="middle", mono=mono)


def flow(pal):
	a = start(pal, "An incoming request: who calls whom", "Time runs down. A bar means the object is busy. Any storage: memory, Redis, JDBC")
	gutter = 152
	widths = [88, 148, 112, 152, 148, 160]
	lanes = [("client", "HTTP", False), ("RateLimitingFilter", "spring/webmvc", True), ("KeyResolver", "whose request", True),
		("DefaultRateLimiter", "the decision", True), ("RateLimitStrategy", "a pure function", True), ("RateLimitRepository", "the counter", True)]
	xs, x = [], gutter
	for w in widths:
		xs.append(x + w / 2)
		x += w
	top, hh = 112, 52
	for (name, role, mono), cx, w in zip(lanes, xs, widths):
		a.rect(cx - (w - 8) / 2, top, w - 8, hh, "card", r=6)
		a.t(cx, top + 22, name, size=11 if mono else 13, color="ink", weight="600", mono=mono, anchor="middle")
		a.t(cx, top + 40, role, size=CAPTION, color="soft", anchor="middle")

	def section(y, name, note):
		a.t(L, y + 4, name, size=13, color="ink", weight="600")
		a.t(L, y + 22, note, size=12)

	def busy(i, y1, y2, hue="blue"):
		a.rect(xs[i] - 4, y1, 8, y2 - y1, hue, r=2)

	step = 34
	y = top + hh + 44
	section(y, "acquired", "tokens are left")
	busy(1, y - 4, y + 11 * step + 4)
	busy(3, y + 3 * step - 4, y + 10 * step + 4)
	msg(a, xs, 0, 1, y, "GET /api/orders", mono=True)
	msg(a, xs, 1, 2, y + step, "resolve()")
	msg(a, xs, 2, 1, y + 2 * step, "alice")
	msg(a, xs, 1, 3, y + 3 * step, "allow(\"api:alice\")")
	msg(a, xs, 3, 5, y + 4 * step, "findByKey")
	msg(a, xs, 5, 3, y + 5 * step, "tokens 3 · v7", mono=False)
	msg(a, xs, 3, 4, y + 6 * step, "allow(1, now)")
	msg(a, xs, 4, 3, y + 7 * step, "tokens 2 · acquired", mono=False)
	msg(a, xs, 3, 5, y + 8 * step, "save(v8)")
	msg(a, xs, 5, 3, y + 9 * step, "saved", mono=False)
	msg(a, xs, 3, 1, y + 10 * step, "acquired · remaining 2", mono=False)
	msg(a, xs, 1, 0, y + 11 * step, "200")

	y += 12 * step + 40
	section(y, "race", "the version changed")
	busy(3, y - 4, y + 5 * step + 4)
	msg(a, xs, 3, 5, y, "save(v8)")
	msg(a, xs, 5, 3, y + step, "conflict: already v8", mono=False, color="amber")
	msg(a, xs, 3, 5, y + 2 * step, "findByKey")
	msg(a, xs, 3, 4, y + 3 * step, "allow(1, now)")
	msg(a, xs, 3, 5, y + 4 * step, "save(v9)")
	msg(a, xs, 5, 3, y + 5 * step, "saved", mono=False)

	y += 6 * step + 40
	section(y, "rejected", "no tokens left")
	busy(3, y - 4, y + 4 * step + 4)
	msg(a, xs, 3, 5, y, "findByKey")
	msg(a, xs, 5, 3, y + step, "tokens 0 · v9", mono=False)
	msg(a, xs, 3, 4, y + 2 * step, "allow(1, now)")
	msg(a, xs, 4, 3, y + 3 * step, "rejected · 4 s", mono=False, color="red")
	msg(a, xs, 3, 1, y + 4 * step, "rejected · nothing to save", mono=False)
	msg(a, xs, 1, 0, y + 5 * step, "429", color="red")
	bottom = y + 5 * step + 16
	for cx in xs:
		a.parts.insert(0, f'<rect x="{cx - 0.5}" y="{top + hh}" width="1" height="{bottom - top - hh}" fill="{a.p["slot"]}"/>')
	return finish(a, bottom)


# ---------------------------------------------------------------- 3. algorithms
LIMIT, WINDOW = 4, 1.0
SERIES = [0.15, 0.2, 0.75, 0.8, 0.85, 0.9, 0.95, 1.05, 1.1, 1.15, 1.2, 1.25, 1.7, 2.3, 2.5, 2.7]


def token_bucket(ts):
	tokens, last, out = float(LIMIT), 0.0, []
	for t in ts:
		tokens = min(LIMIT, tokens + (t - last) * LIMIT / WINDOW)
		last = t
		ok = tokens >= 1
		if ok:
			tokens -= 1
		out.append((t, t if ok else None))
	return out


def leaky_bucket(ts):
	interval, tat, out = WINDOW / LIMIT, 0.0, []
	for t in ts:
		start_at = max(tat, t)
		if start_at - t <= (LIMIT - 1) * interval + 1e-9:
			tat = start_at + interval
			out.append((t, start_at))
		else:
			out.append((t, None))
	return out


def fixed_window(ts):
	counts, out = {}, []
	for t in ts:
		w = int(t // WINDOW)
		ok = counts.get(w, 0) < LIMIT
		if ok:
			counts[w] = counts.get(w, 0) + 1
		out.append((t, t if ok else None))
	return out


def sliding_log(ts):
	log, out = [], []
	for t in ts:
		log = [x for x in log if x > t - WINDOW]
		ok = len(log) < LIMIT
		if ok:
			log.append(t)
		out.append((t, t if ok else None))
	return out


def sliding_counter(ts):
	counts, out = {}, []
	for t in ts:
		w = int(t // WINDOW)
		overlap = 1 - (t - w * WINDOW) / WINDOW
		estimate = counts.get(w, 0) + counts.get(w - 1, 0) * overlap
		ok = estimate + 1 <= LIMIT + 1e-9
		if ok:
			counts[w] = counts.get(w, 0) + 1
		out.append((t, t if ok else None))
	return out


def algorithms(pal):
	a = start(pal, "Five algorithms on one series of requests", "The limit is 4 requests a second. Green is acquired, red is rejected, amber waited for its turn")
	x0, px, h = 340, 200, 20
	rows = [
		("TokenBucket", "a burst up to the limit, then", "a steady refill", token_bucket),
		("LeakyBucket", "a queue up to the limit,", "out at exactly 4 a second", leaky_bucket),
		("FixedWindow", "a counter per window; twice", "the limit gets through at the edge", fixed_window),
		("SlidingWindowLog", "exact: a log of timestamps,", "memory grows with the limit", sliding_log),
		("SlidingWindowCounter", "two windows and their overlap:", "nearly exact and cheap", sliding_counter),
	]
	y = 120
	for name, n1, n2, f in rows:
		a.name(L, y + 6, name, mono=True)
		a.t(L, y + 26, n1)
		a.t(L, y + 44, n2)
		base = y + 24
		a.rect(x0, base + h / 2, 3 * px, 1, "slot", r=0)
		for s in (1, 2):
			a.rect(x0 + s * px, y - 6, 1, h + 40, "slot", r=0)
		for t, served in f(SERIES):
			x = x0 + t * px
			if served is None:
				a.rect(x - 2, base, 4, h, "red", r=1)
			elif served - t > 1e-6:
				a.rect(x - 2, base + h / 2 - 1, (served - t) * px, 2, "amber", r=1)
				a.rect(x - 2, base + h / 2 - 3, 4, 6, "amber", r=1)
				a.rect(x0 + served * px - 2, base, 4, h, "green", r=1)
			else:
				a.rect(x - 2, base, 4, h, "green", r=1)
		if name == "FixedWindow":
			a.path(f"M{x0 + 0.75 * px},{base + h + 8} L{x0 + 0.75 * px},{base + h + 12} L{x0 + 1.2 * px},{base + h + 12} L{x0 + 1.2 * px},{base + h + 8}")
			a.t(x0 + 0.975 * px, base + h + 28, "8 requests in 0.45 s", size=CAPTION, color="ink", anchor="middle")
		y += 76
	axis = y - 20
	for s in range(4):
		a.t(x0 + s * px, axis, f"{s} s", size=AXIS, color="mute", anchor="start" if s == 0 else "middle")
	notes = [
		(0, "start", "the quota is full,", "all get through"),
		(0.75, "a burst at the edge", "10 requests in 0.5 s:", "here the algorithms differ"),
		(1.6, "after the burst", "FixedWindow and the log", "still count the burst"),
	]
	y = axis + 36
	for t, head, n1, n2 in notes:
		a.t(x0 + t * px, y, head, size=13, color="ink", weight="600")
		a.t(x0 + t * px, y + 20, n1, size=12)
		a.t(x0 + t * px, y + 38, n2, size=12)
	return finish(a, y + 38)


# ---------------------------------------------------------------- 4. incoming
def flow_card(a, x, y, w, h, hue, name, lines, mono=True):
	plate = "card" if hue == "card" else hue + "-plate"
	ink = "ink" if hue == "card" else b.ink(hue)
	soft = "soft" if hue == "card" else hue + "-soft"
	a.rect(x, y, w, h, plate, r=6)
	a.t(x + 16, y + 30, name, size=13, color=ink, weight="600", mono=mono)
	for i, s in enumerate(lines):
		assert fits(s, w - 32, b.NOTE) or s.startswith(" "), s
		a.t(x + 16, y + 56 + i * 18, s, color=soft, mono=s.startswith(" "))


def incoming(pal):
	a = start(pal, "An incoming request", "spring/webmvc: a filter before DispatcherServlet; the limit comes from the path, the key from the request")
	gap = 32
	w = (R - L - 2 * gap) / 3
	y, h = 112, 112
	flow_card(a, L, y, w, h, "green", "RateLimitingFilter", ["a rule by path: /api/** →", "limit api, key principal"])
	flow_card(a, L + w + gap, y, w, h, "green", "KeyResolver", ["principal, IP, a header", "X-Tenant-Id or a lambda"])
	flow_card(a, L + 2 * (w + gap), y, w, h, "blue", "RateLimiter", ["allow(\"api:alice\", 1)"])
	a.harrow(L + w + 6, y + h / 2, L + w + gap - 6)
	a.harrow(L + 2 * w + gap + 6, y + h / 2, L + 2 * (w + gap) - 6)
	y2, h2 = y + h + 48, 152
	half = (R - L - gap) / 2
	a.t(L, y2 - 12, "acquired", size=13, color="ink", weight="500")
	a.t(L + half + gap, y2 - 12, "rejected", size=13, color="ink", weight="500")
	ok = ["HTTP/1.1 200 OK", "RateLimit-Policy: \"api\";q=100;w=60", "RateLimit: \"api\";r=37;t=21", "", "the request goes on down the chain"]
	no = ["HTTP/1.1 429 Too Many Requests", "Retry-After: 4", "RateLimit: \"api\";r=0;t=4", "Content-Type: application/problem+json", "RateLimitRejectionHandler"]
	for x, hue, rows in ((L, "green", ok), (L + half + gap, "red", no)):
		a.rect(x, y2, half, h2, "card", r=6)
		a.rect(x, y2, 4, h2, hue, r=2)
		for i, s in enumerate(rows):
			a.t(x + 20, y2 + 28 + i * 24, s, size=12, color="ink" if i == 0 else "soft", mono=bool(s) and i < 4)
	sx = L + 2 * (w + gap) + w / 2
	a.path(f"M{sx},{y + h + 6} L{sx},{y + h + 20} L{L + half / 2},{y + h + 20} L{L + half / 2},{y2 - 28}")
	a.path(f"M{sx},{y + h + 20} L{L + half + gap + half / 2},{y + h + 20} L{L + half + gap + half / 2},{y2 - 28}")
	bottom = y2 + h2 + 28
	a.t(L, bottom, "RateLimit headers follow the IETF draft-ietf-httpapi-ratelimit-headers and are off by default; Retry-After follows RFC 9110 and is always sent.", size=12)
	return finish(a, bottom)


# ---------------------------------------------------------------- 5. outgoing
def outgoing(pal):
	a = start(pal, "Outgoing calls to a partner", "spring/web: the partner allows 4 calls a second. The application sends 8 calls at once, maxWait is 1.5 s")
	x0, px, h, pitch = 320, 300, 22, 88
	top = 128
	rows = [
		("no limiter", "8 calls go out at once,", "the partner answers 429 to 4"),
		("LeakyBucket", "a queue: a call goes out", "every 0.25 s; the 8th would wait 1.75 s"),
		("TokenBucket", "4 at once, the rest wait", "for the bucket to refill"),
	]
	plans = [
		[(0, "green")] * 4 + [(0, "red")] * 4,
		[(i * 0.25, "green") for i in range(7)] + [(1.75, "red")],
		[(0, "green")] * 4 + [(0.25 * k, "green") for k in range(1, 5)],
	]
	for index, ((name, n1, n2), plan) in enumerate(zip(rows, plans)):
		y = top + index * pitch
		a.t(L, y + 6, name, size=13, color="ink", weight="600", mono=index > 0)
		a.t(L, y + 26, n1)
		a.t(L, y + 44, n2)
		base = y + 10
		a.rect(x0, base + h, 2 * px, 1, "slot", r=0)
		stacked = 0
		for t, hue in plan:
			if t == 0:
				x = x0 + stacked * 7
				stacked += 1
			elif hue == "red":
				x = x0 + t * px
			else:
				x = x0 + t * px
				a.rect(x0, base + h / 2 - 1, x - x0, 2, "amber", r=1)
			a.rect(x - 2, base, 4, h, hue, r=1)
	bottom_rows = top + 3 * pitch - 40
	limit = x0 + 1.5 * px
	a.rect(limit, top - 12, 1.5, bottom_rows - top + 20, "ink", r=0)
	a.t(limit + 8, top - 2, "maxWait", size=CAPTION, color="ink", mono=True)
	axis = bottom_rows + 28
	for s in (0, 0.5, 1, 1.5, 2):
		a.t(x0 + s * px, axis, f"{s:g} s", size=AXIS, color="mute", anchor="start" if s == 0 else "middle")
	notes = [
		(0, "a call waits", "the delay of LeakyBucket", "or retryAfter of TokenBucket"),
		(0.75, "waited too long", "RateLimitExceededException,", "and the call is not sent"),
		(1.55, "the key is the partner", "in Redis: one quota", "for all instances"),
	]
	y = axis + 36
	for t, head, n1, n2 in notes:
		a.t(x0 + t * px if t else L, y, head, size=13, color="ink", weight="600")
		a.t(x0 + t * px if t else L, y + 20, n1, size=12)
		a.t(x0 + t * px if t else L, y + 38, n2, size=12)
	return finish(a, y + 38)


# ---------------------------------------------------------------- 5a. annotation
def annotation(pal):
	a = start(pal, "A method with @RateLimit", "spring/context: a proxy before the method; the limiter comes by name, the key from SpEL")
	gap = 32
	w = (R - L - 2 * gap) / 3
	y, h = 112, 112
	flow_card(a, L, y, w, h, "green", "@RateLimit", [" limiter = \"partner-acme\"", " key = \"#tenantId\"", " maxWait = \"500ms\""])
	flow_card(a, L + w + gap, y, w, h, "green", "RateLimitMethodInterceptor", ["a proxy made by", "RateLimitBeanPostProcessor;", "SpEL turns #tenantId into t-42"])
	flow_card(a, L + 2 * (w + gap), y, w, h, "blue", "RateLimiter", [" acquire(\"t-42\", 1, 500ms)", "found by name in", "RateLimiterRegistry"])
	a.harrow(L + w + 6, y + h / 2, L + w + gap - 6)
	a.harrow(L + 2 * w + gap + 6, y + h / 2, L + 2 * (w + gap) - 6)
	y2, h2 = y + h + 48, 152
	half = (R - L - gap) / 2
	a.t(L, y2 - 12, "acquired", size=13, color="ink", weight="500")
	a.t(L + half + gap, y2 - 12, "rejected", size=13, color="ink", weight="500")
	ok = [("the method runs", False), ("one permit of t-42 is taken", False), ("", False), ("the proxy returns what the method returned", False)]
	no = [("RateLimitExceededException", True), ("retryAfter = PT0.75S", True), ("", False),
		("from a controller: 429 and Retry-After, by spring/webmvc", False), ("elsewhere: the caller gets the exception", False)]
	for x, hue, rows in ((L, "green", ok), (L + half + gap, "red", no)):
		a.rect(x, y2, half, h2, "card", r=6)
		a.rect(x, y2, 4, h2, hue, r=2)
		for i, (text, mono) in enumerate(rows):
			assert fits(text, half - 40, 12), text
			a.t(x + 20, y2 + 28 + i * 24, text, size=12, color="ink" if i == 0 else "soft", mono=mono)
	sx = L + 2 * (w + gap) + w / 2
	a.path(f"M{sx},{y + h + 6} L{sx},{y + h + 20} L{L + half / 2},{y + h + 20} L{L + half / 2},{y2 - 28}")
	a.path(f"M{sx},{y + h + 20} L{L + half + gap + half / 2},{y + h + 20} L{L + half + gap + half / 2},{y2 - 28}")
	bottom = y2 + h2 + 28
	a.t(L, bottom, "A call from inside the same bean skips the proxy, as with @Transactional. An unknown limiter name stops the application at startup.", size=12)
	a.t(L, bottom + 18, "The proxy is applied before the one of @Retryable, so the retry wraps it and every attempt takes a permit.", size=12)
	return finish(a, bottom + 18)


# ---------------------------------------------------------------- 6. shared counter
def pointer(a, x1, y1, x2, y2, color="line", label=None):
	a.path(f"M{x1},{y1} L{x2 + 10},{y2}", color=color, width=1.5)
	dx, dy = x2 - x1, y2 - y1
	n = (dx * dx + dy * dy) ** 0.5
	ux, uy = dx / n, dy / n
	bx, by = x2 - ux * 8, y2 - uy * 8
	a.add(f'<path d="M{x2},{y2} L{bx - uy * 4},{by + ux * 4} L{bx + uy * 4},{by - ux * 4} Z" fill="{a.p[color]}"/>')
	if label:
		a.t((x1 + x2) / 2, (y1 + y2) / 2 - 10, label, size=CAPTION, color="soft", anchor="middle", mono=True)


def shared(pal):
	a = start(pal, "One counter for several instances", "No coordinator: each instance reads the version and writes only if it has not changed")
	top = 112
	a.name(L, top + 14, "storage", mono=False)
	a.t(L, top + 34, "Redis here; the key is the limit name and the request key, the lifetime is expiresAt", size=CAPTION, color="mute")
	rows0, group = top + 72, 104
	records = [
		("traffic:api:alice", "v9 · tokens 1", "blue"),
		("traffic:partner:acme", "v120 · queue until 12:00:01.750", "blue"),
		("traffic:api:bob", "v3 · tokens 4 · expires in 1 min", "amber"),
	]
	ends = []
	for g, (key, value, hue) in enumerate(records):
		gy = rows0 + g * group
		a.record(L, gy + 3, hue)
		a.t(L + 16, gy + 11, key, size=12, color="ink", mono=True)
		a.t(L + 16, gy + 31, value, size=CAPTION, color="soft", mono=True)
		ends.append((L + 16 + mono_w(value if len(value) > len(key) else key, 12) + 16, gy + 12))
	a.t(ends[2][0] + 16, rows0 + 2 * group + 16, "nobody calls, so storage removes the key itself", color="mute")
	cx, cw, ch = 640, 320, 72
	instances = [
		("instance A", "v7 → v8: saved at the first attempt", rows0 - 20),
		("instance B", "v7 → conflict; read v8 again → v9", rows0 - 20 + group * 0.9),
	]
	for name, note, y in instances:
		a.rect(cx, y, cw, ch, "blue-plate", r=6)
		b.tag(a, cx + 12, y + 12, name, color="blue-soft")
		a.t(cx + 12, y + 56, note, size=12, color="blue-ink")
	ax, ay = ends[0]
	pointer(a, cx - 4, instances[0][2] + 32, ax + 10, ay - 4, label="save(v8)")
	pointer(a, cx - 4, instances[1][2] + 24, ax + 10, ay + 6, color="amber")
	pointer(a, cx - 4, instances[1][2] + 52, ends[1][0], ends[1][1], label="LeakyBucket: wait 0.5 s")
	bottom = rows0 + 3 * group - 24
	a.t(L, bottom, "save compares the version and writes in one step: a Lua script with PX in Redis, UPDATE … WHERE version = ? in JDBC, compute in memory.", size=12)
	return finish(a, bottom)


# ---------------------------------------------------------------- 7. failures
def bracket(a, x1, x2, y, label):
	a.path(f"M{x1},{y - 4} L{x1},{y} L{x2},{y} L{x2},{y - 4}", color="line", width=1)
	a.t((x1 + x2) / 2, y + 16, label, size=CAPTION, color="mute", anchor="middle", mono=True)


def marker(a, x, top, bottom, label, mono=True):
	a.rect(x, top, 1.5, bottom - top, "ink", r=0)
	a.t(x + 8, top + 10, label, size=CAPTION, color="ink", mono=mono)


def failures(pal):
	a = start(pal, "Storage failure, race, expiry and restart", "DefaultRateLimiter decides, not an adapter, so incoming and outgoing calls behave alike")
	x0, bar = 320, 24
	rows = [
		("storage is down", "the call goes through, WARN in the log;", "reject turns it down"),
		("a hot key", "version conflicts in a row:", "after max-attempts, as a failure"),
		("a key is no longer used", "the counter is full again,", "so storage removes it"),
		("the process restarts", "the counter in memory is lost,", "the limit starts over"),
	]
	y = 112
	for index, (name, n1, n2) in enumerate(rows):
		a.t(L, y + 34, name, size=13, color="ink", weight="600")
		a.t(L, y + 54, n1)
		a.t(L, y + 72, n2)
		by = y + 24
		if index == 0:
			a.rect(x0, by + 8, 600, 8, "red", r=2)
			a.t(x0, by - 8, "Redis does not answer", size=CAPTION, color="ink")
			for k in range(12):
				a.rect(x0 + 12 + k * 48, by, 3, bar, "green", r=1)
			bracket(a, x0, x0 + 600, by + bar + 8, "on-failure: allow")
		elif index == 1:
			for k in range(3):
				a.rect(x0 + k * 56, by, 48, bar, "amber", r=2)
			a.rect(x0 + 168, by, 48, bar, "green", r=2)
			a.t(x0 + 232, by + 16, "saved at the 4th attempt", color="mute")
			bracket(a, x0, x0 + 448, by + bar + 8, "max-attempts: 8")
		elif index == 2:
			a.rect(x0, by, 160, bar, "blue", r=2)
			a.rect(x0 + 162, by + bar / 2 - 1, 238, 2, "bar", r=1)
			marker(a, x0 + 400, by - 20, by + bar + 4, "removed")
			bracket(a, x0 + 160, x0 + 400, by + bar + 8, "expiresAt")
		else:
			a.rect(x0, by, 200, bar, "blue", r=2)
			marker(a, x0 + 200, by - 20, by + bar + 4, "restart", mono=False)
			a.rect(x0 + 204, by, 200, bar, "sky", r=2)
			a.t(x0 + 420, by + 16, "InMemory: a full quota", color="mute")
		y += 96 + 32
	return finish(a, y - 32)


# ---------------------------------------------------------------- 8. wiring
def wiring(pal):
	a = start(pal, "Wiring without Spring and in Spring", "The core is the same: only who creates the limits and attaches them to calls differs")
	gx, gap = 200, 16
	cw = (R - gx - gap) / 2
	ax, sx = gx, gx + cw + gap
	head = 120
	a.t(ax, head, "without Spring", size=13, color="ink", weight="600")
	a.t(sx, head, "Spring", size=13, color="ink", weight="600")
	rows = [
		("strategy", ("blue", "TokenBucket.of(100, Duration.ofMinutes(1))", "in the application code"),
			("amber", "semantic.traffic.limiters.api.*", "in application.yaml")),
		("limiter", ("blue", "DefaultRateLimiter(\"api\", strategy, repository)", "the same class from semantic-traffic"), None),
		("storage", ("blue", "InMemoryRateLimitRepository()", "or your own RateLimitRepository"),
			("green", "RedisRateLimitRepository", "with starter/redis on the classpath")),
		("incoming", ("blue", "limiter.allow(key)", "in your code, the answer is yours"),
			("green", "RateLimitingFilter", "rules by path from the settings")),
		("outgoing", ("blue", "limiter.acquire(key, maxWait = …)", "before a call to a partner"),
			("green", "RateLimitingInterceptor", "in RestClient.Builder by limit name")),
		("a method", ("blue", "registry.findByName(\"partner-acme\")", "then acquire around the call, by hand"),
			("green", "@RateLimit(\"partner-acme\")", "a proxy from the post-processor")),
	]
	y, h = head + 20, 76
	for label, left, right in rows:
		a.t(L, y + 32, label, size=13, color="ink", weight="500")
		cells = [(ax, 2 * cw + gap, left)] if right is None else [(ax, cw, left), (sx, cw, right)]
		for x, w, (hue, code, note) in cells:
			assert mono_w(code, 13) <= w - 24 and fits(note, w - 24, b.NOTE), code
			a.rect(x, y, w, h, hue + "-plate", r=6)
			a.name(x + 12, y + 32, code, mono=True, color=b.ink(hue))
			a.t(x + 12, y + 56, note, color=hue + "-soft")
		y += h + gap
	return finish(a, y - gap)


# ---------------------------------------------------------------- 9. extension points
def chip_card(a, x, y, w, hue, name, note, mono=True):
	h = 56
	if hue == "ghost":
		a.rect(x, y, w, h, "ghost", r=6)
		a.outline(x, y, w, h, dash="2 3")
		ink, soft = "ink", "soft"
	else:
		a.rect(x, y, w, h, hue + "-plate", r=6)
		ink, soft = b.ink(hue), hue + "-soft"
	a.t(x + 12, y + 24, name, size=12, color=ink, weight="600", mono=mono)
	a.t(x + 12, y + 42, note, size=CAPTION, color=soft)


def extensions(pal):
	a = start(pal, "What can be replaced without touching the rest", "Each point is an interface; the algorithms are sealed, since any storage must be able to save their state")
	gx = 200
	groups = [
		("algorithm", "RateLimitStrategy", "sealed: the five of Alex Xu, no others", [
			("blue", "TokenBucket", "the default", True),
			("blue", "LeakyBucket", "a steady pace", True),
			("blue", "FixedWindow", "the cheapest", True),
			("blue", "SlidingWindowLog", "exact", True),
			("blue", "SlidingWindowCounter", "nearly exact", True)]),
		("storage", "RateLimitRepository", "findByKey and save with a version check", [
			("blue", "InMemory…", "the default", True),
			("green", "Redis…", "spring/data/redis", True),
			("ghost", "Jdbc…", "spring/jdbc, when needed", True),
			("ghost", "your own", "Hazelcast and the like", False)]),
		("key", "KeyResolver", "fun interface: request → key", [
			("green", "principal", "the default", False),
			("green", "remote address", "the client IP", False),
			("green", "header", "X-Tenant-Id", False),
			("ghost", "a lambda", "in the application code", False)]),
		("method", "@RateLimit", "the key of a method call is a SpEL expression", [
			("green", "#tenantId", "a parameter", True),
			("green", "#order.customerId", "a property of a parameter", True),
			("green", "@tenants.current()", "a bean", True),
			("ghost", "no key", "one counter for the limiter", False)]),
		("rejected", "RateLimitRejectionHandler", "fun interface: what to answer", [
			("green", "ProblemDetail", "429 after RFC 9457", False),
			("green", "RateLimitExceededException", "of @RateLimit, 429 as well", True),
			("ghost", "your own", "another response format", False)]),
	]
	y = 112
	for label, name, note, chips in groups:
		a.t(L, y + 28, label, size=13, color="ink", weight="500")
		a.rect(gx, y, R - gx, 64, "card", r=6)
		a.name(gx + 12, y + 28, name, mono=True)
		a.t(gx + 12, y + 48, note)
		cy, x = y + 64 + 12, gx
		for hue, cname, cnote, mono in chips:
			w = max(mono_w(cname, 12) if mono else len(cname) * 12 * 0.56, len(cnote) * CAPTION * 0.56) + 24
			chip_card(a, x, cy, w, hue, cname, cnote, mono)
			x += w + 12
		assert x - 12 <= R, label
		y = cy + 56 + 32
	return finish(a, y - 32)


# ---------------------------------------------------------------- 10. plan
def plan(pal):
	a = start(pal, "Pull requests: what runs in sequence and what in parallel", "An arrow means it needs that one merged. A column is a stage; everything in one column runs in parallel")
	cols = [("stage 1", "in sequence"), ("stage 2", "in parallel"), ("stage 3", "after adapters"), ("stage 4", "after all")]
	cx = [L, L + 232, L + 464, L + 696]
	cw, ch, gap = 216, 52, 12
	for (name, note), x in zip(cols, cx):
		a.t(x, 112, name, size=13, color="ink", weight="600")
		a.t(x + 64, 112, note, size=12)
	top = 132
	cards = {
		1: (0, 0, "blue", "1 · core", "the contract"),
		2: (0, 1, "blue", "2 · core", "TokenBucket"),
		3: (0, 2, "blue", "3 · core", "the counter in memory"),
		4: (0, 3, "blue", "4 · core", "DefaultRateLimiter"),
		5: (0, 4, "blue", "5 · core", "serialization of the state"),
		6: (1, 0, "blue", "6 · core", "acquire(maxWait)"),
		7: (1, 1, "blue", "7 · core", "RateLimiterRegistry"),
		8: (1, 2, "blue", "8 · observability", "micrometer"),
		9: (1, 3, "green", "9 · spring/webmvc", "filter, 429, headers"),
		10: (1, 4, "blue", "10 · core", "FixedWindow"),
		11: (1, 5, "blue", "11 · core", "SlidingWindowLog"),
		12: (1, 6, "blue", "12 · core", "SlidingWindowCounter"),
		13: (1, 7, "blue", "13 · core", "LeakyBucket and delay"),
		14: (1, 8, "green", "14 · spring/data/redis", "the shared counter"),
		15: (2, 0, "green", "15 · spring/web", "RestClient interceptor"),
		16: (2, 1, "green", "16 · spring/context", "@RateLimit and its proxy"),
		17: (3, 2, "amber", "17 · autoconfigure", "limits, rules, the proxy"),
		18: (3, 3, "amber", "18 · starter", "starter and starter/redis"),
	}
	pos = {}
	for k, (col, row, hue, name, note) in cards.items():
		x, y = cx[col], top + row * (ch + gap)
		pos[k] = (x, y)
		a.rect(x, y, cw, ch, hue + "-plate", r=6)
		a.t(x + 12, y + 22, name, size=12, color=b.ink(hue), weight="600", mono=True)
		a.t(x + 12, y + 40, note, size=CAPTION, color=hue + "-soft")
	ghost_y = top + 9 * (ch + gap) + 24
	for i, (name, note) in enumerate((("spring/jdbc", "when a service needs it"), ("concurrency-limit", "a design of its own, later"))):
		x = cx[2 + i]
		a.rect(x, ghost_y, cw, ch, "ghost", r=6)
		a.outline(x, ghost_y, cw, ch, dash="2 3")
		a.t(x + 12, ghost_y + 22, name, size=12, color="ink", weight="600", mono=True)
		a.t(x + 12, ghost_y + 40, note, size=CAPTION)

	def edge(src, dst, label=None):
		sx, sy = pos[src]
		dx, dy = pos[dst]
		x1, y1 = sx + cw, sy + ch / 2
		x2, y2 = dx, dy + ch / 2
		if sx == dx:
			x = sx + 24
			b.dot(a, x, sy + ch + b.AIR + b.DOT)
			_, by = b.arrowhead(a, x, dy - b.AIR, 0, 1)
			b.line(a, f"M{x},{sy + ch + b.AIR + b.DOT} L{x},{by}", b.DECLARES)
			return
		mid = x1 + 8
		start_x = x1 + b.AIR + b.DOT
		b.dot(a, start_x, y1)
		bx, _ = b.arrowhead(a, x2 - b.AIR, y2, 1, 0)
		r = 4 if y2 > y1 else (-4 if y2 < y1 else 0)
		if r:
			b.line(a, f"M{start_x},{y1} L{mid - 4},{y1} Q{mid},{y1} {mid},{y1 + r} L{mid},{y2 - r} Q{mid},{y2} {mid + 4},{y2} L{bx},{y2}", b.DECLARES)
		else:
			b.line(a, f"M{start_x},{y1} L{bx},{y2}", b.DECLARES)

	for k in (1, 2, 3, 4):
		edge(k, k + 1)
	for k in (6, 7, 8, 9):
		edge(1, k)
	for k in (10, 11, 12, 13, 14):
		edge(5, k)
	edge(6, 15)
	edge(6, 16)
	edge(7, 16)
	edge(9, 17)
	edge(15, 17)
	edge(16, 17)
	edge(17, 18)
	bottom = ghost_y + ch + 32
	a.t(L, bottom, "10–13 add a strategy and its format to the serialization of PR 5. 6–9 need only the contract of PR 1.", size=12, color="ink")
	a.t(L, bottom + 18, "Shared files are settings.gradle.kts, docs/diagrams/traffic.py and the README: each PR adds a line, conflicts are settled on rebase.", size=12)
	return finish(a, bottom + 18)


# ---------------------------------------------------------------- modules
MODULES = d.Map(
	3,
	[
		("JVM", "no Spring", [0]),
		("Spring", "an annotation, a server filter, a client interceptor, Redis", [1, 2]),
		("Spring Boot", "wiring only", [3]),
		("later", "a design of its own", [4]),
	],
	{"JVM": "blue", "Spring": "green", "Spring Boot": "amber", "later": "card"},
	[
		(0, [0, 1], "core", ["RateLimiter", "DefaultRateLimiter", "RateLimitStrategy", "RateLimitRepository", "RateLimiterRegistry", "RateLimitDecision"],
			"The contract, the five algorithms, counters and limiters in memory. No direction: one limiter for incoming and outgoing calls."),
		(0, [2], "observability/micrometer", [], "Permits acquired and rejected, by limiter name and never by key."),
		(1, [0], "spring/context", ["@RateLimit", "@EnableRateLimiting"], "Methods of beans: a proxy takes a permit before the call, the key comes from SpEL."),
		(1, [1], "spring/webmvc", ["KeyResolver", "OncePerRequestFilter"], "Incoming requests: a key per request, 429 with Retry-After and RateLimit headers."),
		(1, [2], "spring/web", ["ClientHttpRequestInterceptor"], "Outgoing calls of RestClient: waits up to a limit for a permit, so a partner's quota holds."),
		(2, [0, 1, 2], "spring/data/redis", ["RedisRateLimitRepository"], "One counter for all instances: save compares the version in a Lua script."),
		(3, [0], "spring-boot/autoconfigure", [], "Limiters by name from properties, the filter, the interceptor and the post-processor of @RateLimit."),
		(3, [1], "spring-boot/starter", [], "One dependency; starter/redis adds the shared counter."),
		(4, [0, 1, 2], "concurrency-limit/core", ["VegasLimit", "Gradient2Limit", "AIMDLimit"], "An adaptive limit of concurrent requests and shedding by priority, after Netflix concurrency-limits.", True),
	],
	lane=56,
)


def modules(pal):
	m = MODULES
	a = b.Art(m.width, m.height, pal)
	d.header(a, "TRAFFIC", "paths under architecture/pattern/traffic; the core has no framework, the rest plugs into its SPI")
	m.draw(a)
	t, c, w = m.w / 3, m.c, m.w
	m.across(a, c[2], c[1] + w, m.top[0] + m.rh[0] / 2, d.DECLARES, "api")
	m.up(a, c[0] + t, 1, 0, d.DECLARES, "api")
	m.up(a, c[1] + t, 1, 0, d.DECLARES, "api")
	m.bend(a, c[2] + t, 1, c[1] + 2 * t, 0, d.DECLARES, "api")
	x, lane = c[0] + t / 2, c[0] - 24
	start_y, low, high = m.top[2] - 5, m.top[2] - d.ROW_GAP / 2, m.bottom(0) + d.ROW_GAP / 2
	d.dot(a, x, start_y)
	_, by = d.head(a, x, m.bottom(0) + 3, 0, -1)
	d.line(a, f"M{x},{start_y} L{x},{low + 8} Q{x},{low} {x - 8},{low} L{lane + 8},{low} Q{lane},{low} {lane},{low - 8} "
		f"L{lane},{high + 8} Q{lane},{high} {lane + 8},{high} L{x - 8},{high} Q{x},{high} {x},{high - 8} L{x},{by}", d.BUILDS)
	d.along(a, lane - 12, (m.top[1] + m.bottom(1)) / 2, "implements RateLimitRepository")
	m.up(a, c[0] + t, 3, 2, d.CONFIGURES, "configures")
	m.across(a, c[1], c[0] + w, m.top[3] + m.rh[3] / 2, d.DECLARES, "api")
	return a


DIAGRAMS = (
	("traffic", modules), ("traffic-duties", duties), ("traffic-abstractions", abstractions), ("traffic-flow", flow),
	("traffic-algorithms", algorithms), ("traffic-incoming", incoming), ("traffic-outgoing", outgoing), ("traffic-annotation", annotation),
	("traffic-shared", shared), ("traffic-failures", failures), ("traffic-wiring", wiring),
	("traffic-extensions", extensions), ("traffic-plan", plan),
)


if __name__ == "__main__":
	for theme, pal in b.PALETTE.items():
		for name, draw in DIAGRAMS:
			(b.OUT / f"{name}-{theme}.svg").write_text(draw(pal).svg())
	print(f"written: {2 * len(DIAGRAMS)} files")
