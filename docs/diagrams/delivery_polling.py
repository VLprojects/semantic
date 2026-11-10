#!/usr/bin/env python3
"""Generates docs/assets/delivery-polling-*.svg: the polling container of delivery, its loop, strategies, scaling and wake-up."""

import build as b

W = 1000
L, R = 40, 960
CAPTION, AXIS = 11, 11


def fits(text, width, size):
	return len(text) * size * 0.56 <= width


def start(pal, title, about):
	a = b.Art(W, 0, pal)
	a.t(L, 52, title, size=16, color="ink", weight="600")
	a.t(L, 76, about, size=13, color="soft")
	return a


def finish(a, bottom):
	a.h = bottom + 40
	a.parts.insert(0, f'<rect x="16" y="16" width="{W - 32}" height="{bottom + 8}" rx="12" fill="{a.p["plate"]}"/>')
	return a


BX, BW, LANE = 248, 292, 128
RX = BX + BW + LANE
ROW = b.BLOCK + 48


def card(a, x, y, w, hue, status, name, note):
	assert fits(note, w - 24, b.NOTE), note
	b.block(a, x, y, w, hue, status, name, note)


def down(a, x, y_high, y_low, label, mono=True):
	start_y = y_high + b.AIR + b.DOT
	b.dot(a, x, start_y)
	_, by = b.arrowhead(a, x, y_low - b.AIR, 0, 1)
	b.line(a, f"M{x},{start_y} L{x},{by}", b.DECLARES)
	a.t(x + 20, (y_high + y_low) / 2 + 4, label, size=12, mono=mono)


def leftward(a, y, label, dash, mono=True):
	b.across(a, RX, BX + BW, y, dash)
	a.t(BX + BW + LANE / 2, y - 12, label, anchor="middle", size=12, mono=mono)


def level(a, y, name, artifact):
	a.t(L, y + 12 + 12.5, name, size=13, color="ink", weight="500")
	a.t(L, y + 12 + 12.5 + 20, artifact, size=CAPTION, color="mute", mono=True)


def abstractions(pal):
	a = start(pal, "Polling container: the abstractions", "Top to bottom, the path of a call: from the start of the application to one batch")
	y = [112 + i * ROW for i in range(4)]
	full = 2 * BW + LANE
	level(a, y[0], "Spring", "semantic-delivery-spring")
	level(a, y[1], "JVM", "semantic-delivery")
	card(a, BX, y[0], full, "green", "PR 5", "OutboxPollingContainerLifecycle", "SmartLifecycle: starts and stops the container with the context")
	card(a, BX, y[1], full, "blue", "PR 2, 3", "OutboxPollingContainer", "start, stop, wakeup; keeps from concurrency to max-concurrency pollers")
	card(a, BX, y[2], BW, "blue", "fun interface · PR 1", "OutboxPollingStrategy", "decides when to poll again")
	card(a, RX, y[2], BW, "blue", "internal · PR 2", "OutboxPoller", "loop: batch, pause, batch again")
	card(a, BX, y[3], full, "blue", "exists", "OutboxDispatcher", "delivers a batch: takes records, sends them, saves the outcome")
	mid = b.BLOCK / 2
	leftward(a, y[2] + mid, "calculateDelay()", b.DECLARES)
	down(a, BX + 48, y[0] + b.BLOCK, y[1], "start() · stop()")
	down(a, RX + 48, y[1] + b.BLOCK, y[2], "runs N", mono=False)
	down(a, RX + 48, y[2] + b.BLOCK, y[3], "dispatch()")
	return finish(a, y[3] + b.BLOCK)


def loop(pal):
	a = start(pal, "One poller: batch, pause, batch again", "A thread is busy only during a batch; the pause waits in the queue of ScheduledExecutorService")
	gap = 48
	w = (R - L - 2 * gap) / 3
	y, h = 112, 96
	steps = [
		("dispatch()", "takes up to batch-size records,", "delivers them, returns their count"),
		("calculateDelay(context)", "from the count and the last pause", "decides how long to wait"),
		("schedule(delay)", "puts the poller in the queue", "of the executor, frees the thread"),
	]
	for i, (call, n1, n2) in enumerate(steps):
		x = L + i * (w + gap)
		assert fits(n1, w - 32, b.NOTE) and fits(n2, w - 32, b.NOTE)
		a.rect(x, y, w, h, "card", r=6)
		a.name(x + 16, y + 32, call, mono=True)
		a.t(x + 16, y + 60, n1)
		a.t(x + 16, y + 78, n2)
		if i < 2:
			a.harrow(x + w + 12, y + h / 2, x + w + gap - 12)
	back = y + h + 24
	x1, x3 = L + w / 2, L + 2 * (w + gap) + w / 2
	a.path(f"M{x3},{y + h + 8} L{x3},{back} L{x1},{back} L{x1},{y + h + 14}")
	a.add(f'<path d="M{x1 - 4},{y + h + 15} L{x1},{y + h + 8} L{x1 + 4},{y + h + 15} Z" fill="{a.p["line"]}"/>')
	a.t((x1 + x3) / 2, back + 24, "after delay", anchor="middle", size=12)
	return finish(a, back + 24)


SEQ = [100, 100, 100, 30, 0, 0, 0]
RUN = 0.3


def delays(key):
	if key == "fixed":
		return [1.0] * len(SEQ)
	if key == "active-idle":
		return [0.0 if n == 100 else 1.0 for n in SEQ]
	out, d = [], 0.25
	for n in SEQ:
		if n == 100:
			d = max(0.25, d / 2)
		elif n <= 25:
			d = min(4.0, d * 2)
		out.append(d)
	return out


def strategies(pal):
	a = start(pal, "Three polling strategies on one series of batches", "Batches of up to 100 records: three full, one of 30, then empty. A bar is as high as the records it took")
	x0, px, bar_h = 280, 80, 40
	rows = [
		("FixedPollingStrategy", "always 1 s", "fixed"),
		("ActiveIdlePollingStrategy", "at once after a full batch, else 1 s", "active-idle"),
		("AdaptivePollingStrategy", "full batch: pause / 2, empty: × 2", "adaptive"),
	]
	y = 128
	for index, (name, note, key) in enumerate(rows):
		base = y + bar_h
		a.name(L, y + 14, name, mono=True)
		a.t(L, y + 34, note)
		a.rect(x0, base, R - x0, 1, "slot", r=0)
		t = 0.0
		for n, d in zip(SEQ, delays(key)):
			x = x0 + t * px
			if x + RUN * px > R:
				break
			hh = bar_h * n / 100
			if n:
				a.rect(x + 1, base - hh, RUN * px - 2, hh, "blue", r=1.5)
			else:
				a.rect(x + 1, base - 2, RUN * px - 2, 2, "bar", r=1)
			if index == 0:
				a.t(x + RUN * px / 2, base - hh - 8, str(n), size=CAPTION, color="mute", anchor="middle", mono=True)
			t += RUN + d
		y += bar_h + 40
	axis = y - 40 + 20
	for s in range(0, 9, 2):
		a.t(x0 + s * px, axis, f"{s} s", size=AXIS, color="mute", anchor="start" if s == 0 else "middle")
	return finish(a, axis)


def scaling(pal):
	a = start(pal, "Optional: more pollers under load, fewer when idle", "concurrency 1, max-concurrency 8; 10 000 records arrive at once")
	x0, x1, minutes = 200, R, 10
	px = (x1 - x0) / minutes

	def tx(m):
		return x0 + m * px

	top, h1 = 128, 64
	a.t(L, top + 4, "waiting", size=13, color="ink", weight="500")
	pts = [(0, 1), (0.5, 0.94), (1, 0.78), (1.5, 0.57), (2, 0.36), (2.5, 0.16), (3, 0)]
	line = " ".join(f"L{tx(m):.1f},{top + h1 - v * h1:.1f}" for m, v in pts)
	a.add(f'<path d="M{tx(0)},{top + h1} {line} L{tx(minutes)},{top + h1} Z" fill="{a.p["amber"]}" fill-opacity="0.2"/>')
	a.path(f"M{tx(0)},{top} {line} L{tx(minutes)},{top + h1}", color="amber", width=2)
	a.t(x0 - 8, top + 4, "10 000", size=AXIS, color="mute", anchor="end")
	a.t(x0 - 8, top + h1 + 4, "0", size=AXIS, color="mute", anchor="end")

	top2, unit = top + h1 + 48, 16
	base = top2 + 8 * unit
	a.t(L, top2 + 4, "pollers", size=13, color="ink", weight="500")
	for n in (8, 1):
		yy = base - n * unit
		a.rect(x0, yy, x1 - x0, 1, "slot", r=0)
		a.t(x0 - 8, yy + 4, str(n), size=AXIS, color="mute", anchor="end")
	steps = [(0, 1)] + [(k / 6, k + 1) for k in range(1, 8)] + [(3.2 + k, 7 - k) for k in range(7)]
	d = f"M{tx(0)},{base - unit}"
	for i, (m, n) in enumerate(steps[1:], start=1):
		d += f" L{tx(m):.1f},{base - steps[i - 1][1] * unit} L{tx(m):.1f},{base - n * unit}"
	d += f" L{tx(minutes)},{base - unit}"
	a.path(d, color="blue", width=2)
	a.rect(x0, base, x1 - x0, 1, "line", r=0)

	done = tx(3)
	a.rect(done, top - 8, 1, base - top + 8, "line", r=0)
	a.t(done + 8, top + 4, "all delivered", size=CAPTION)
	a.t(tx(1.35), base - 5 * unit, "+1 poller every 10 s", size=12, color="ink")
	a.t(tx(1.35), base - 5 * unit + 18, "while batches are full", size=12)
	a.t(tx(5.4), base - 7 * unit + 4, "−1 poller every 60 s", size=12, color="ink")
	a.t(tx(5.4), base - 7 * unit + 22, "once batches are no longer full", size=12)

	axis = base + 24
	for m in range(0, minutes + 1, 2):
		a.t(tx(m), axis, f"{m} min", size=AXIS, color="mute", anchor="start" if m == 0 else ("end" if m == minutes else "middle"))
	return finish(a, axis)


def mono_w(text, size):
	return len(text) * size * 0.6


def parallel(pal):
	a = start(pal, "Several pollers and instances, no coordinator", "Each takes its own batch through findAndModifyScheduled; the others skip a record already taken")
	top = 112
	a.name(L, top + 14, "outbox", mono=True)
	a.t(L, top + 34, "by scheduledAt, batch-size 4 to keep it small", size=CAPTION, color="mute")
	rows0, pitch, group = top + 56, 20, 96
	cx, cw = 560, 300
	owners = [("instance A", "poller 1"), ("instance A", "poller 2"), ("instance B", "poller 3"), None]
	for g, owner in enumerate(owners):
		gy = rows0 + g * group
		for r in range(4):
			ry = gy + r * pitch
			a.record(L, ry + 3, "blue" if owner else "amber")
			a.t(L + 16, ry + 11, "PROCESSING" if owner else "PENDING", size=CAPTION, color="soft", mono=True)
			a.t(L + 128, ry + 11, f"12:00:{g * 4 + r:02d}", size=CAPTION, color="mute", mono=True)
		mid = gy + 4 * pitch / 2 - 4
		if owner:
			instance, poller = owner
			a.harrow(L + 220, mid, cx - 12)
			if g == 0:
				a.t((L + 220 + cx) / 2, mid - 10, "findAndModifyScheduled(4)", size=CAPTION, color="soft", anchor="middle", mono=True)
			a.rect(cx, gy - 2, cw, 80, "blue-plate", r=6)
			b.tag(a, cx + 12, gy + 10, instance, color="blue-soft")
			a.t(cx + 12, gy + 48, poller, size=13, color="blue-ink", weight="600")
			a.t(cx + 12, gy + 66, "took 4 records and delivers them", color="blue-soft")
		else:
			a.t(cx, mid + 4, "wait for the next poll", color="mute")
	return finish(a, rows0 + 3 * group + 4 * pitch)


def bracket(a, x1, x2, y, label):
	a.path(f"M{x1},{y - 4} L{x1},{y} L{x2},{y} L{x2},{y - 4}", color="line", width=1)
	a.t((x1 + x2) / 2, y + 16, label, size=CAPTION, color="mute", anchor="middle", mono=True)


def moment(a, x, top, bottom, label, mono=True):
	a.rect(x, top, 1.5, bottom - top, "ink", r=0)
	a.t(x + 8, top + 10, label, size=CAPTION, color="ink", mono=mono)


def failures(pal):
	a = start(pal, "A failed batch, a stop and a crash", "No record is lost in any of the three: at least once")
	x0, bar = 320, 24
	rows = [
		("batch failed", "database is down: logged,", "the loop goes on"),
		("stop() called", "in detail on the diagram", "of graceful shutdown"),
		("process crashed", "records stay PROCESSING,", "a background job returns them"),
	]
	y = 112
	for index, (name, n1, n2) in enumerate(rows):
		a.t(L, y + 34, name, size=13, color="ink", weight="600")
		a.t(L, y + 54, n1)
		a.t(L, y + 72, n2)
		by = y + 24
		if index == 0:
			a.rect(x0, by, 64, bar, "red", r=2)
			bracket(a, x0 + 64, x0 + 224, by + bar + 8, "idle-interval")
			a.rect(x0 + 224, by, 64, bar, "blue", r=2)
			bracket(a, x0 + 288, x0 + 448, by + bar + 8, "idle-interval")
			a.rect(x0 + 448, by, 64, bar, "blue", r=2)
		elif index == 1:
			a.rect(x0, by, 160, bar, "blue", r=2)
			moment(a, x0 + 80, by - 20, by + bar + 4, "stop()")
			bracket(a, x0 + 80, x0 + 320, by + bar + 8, "shutdown-timeout")
			a.t(x0 + 336, by + 16, "no new batches", color="mute")
		else:
			a.rect(x0, by, 80, bar, "blue", r=2)
			moment(a, x0 + 80, by - 20, by + bar + 4, "process stopped", mono=False)
			a.rect(x0 + 82, by + 8, 318, 8, "amber", r=2)
			bracket(a, x0 + 80, x0 + 400, by + bar + 8, "staleness.processing: 10 min")
			a.rect(x0 + 400, by, 80, bar, "blue", r=2)
			a.t(x0 + 496, by + 16, "another poller delivers", color="mute")
		y += 96 + 32
	return finish(a, y - 32)


def wiring(pal):
	a = start(pal, "Without Spring and with Spring", "The core is the same; only who creates the threads and calls start and stop differs")
	gx, gap = 200, 16
	cw = (R - gx - gap) / 2
	ax, sx = gx, gx + cw + gap
	head = 120
	a.t(ax, head, "without Spring", size=13, color="ink", weight="600")
	a.t(sx, head, "Spring", size=13, color="ink", weight="600")
	rows = [
		("threads", ("blue", "Executors.newScheduledThreadPool(8)", "created and closed by the application"),
			("green", "ThreadPoolTaskScheduler", "from auto-configuration, outbox-poller-*")),
		("container", ("blue", "OutboxPollingContainer(dispatcher, executor, properties)", "the same class from semantic-delivery"), None),
		("start and stop", ("blue", "container.start() · container.stop()", "called by the application on start and stop"),
			("green", "OutboxPollingContainerLifecycle", "with the context, waits up to shutdown-timeout")),
		("settings", ("blue", "OutboxProperties(polling = ...)", "in the application code"),
			("green", "semantic.delivery.outbox.polling.*", "in application.yaml")),
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
	a = start(pal, "What can be replaced without touching the rest", "Each point is an interface; a new implementation changes no code around it")
	gx = 200
	groups = [
		("pause", "OutboxPollingStrategy", "calculateDelay(context): Duration", [
			("blue", "FixedPollingStrategy", "constant pause", True),
			("blue", "ActiveIdlePollingStrategy", "default", True),
			("blue", "AdaptivePollingStrategy", "grows when idle", True),
			("ghost", "own lambda", "in the application", False)]),
		("threads", "ScheduledExecutorService", "JDK: the pause waits in a queue, not in a thread", [
			("blue", "newScheduledThreadPool(n)", "JDK pool", True),
			("blue", "Thread.ofVirtual()", "virtual threads", True),
			("green", "ThreadPoolTaskScheduler", "Spring", True)]),
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
			x += w + 16
		assert x - 16 <= R, label
		y = cy + 56 + 40
	return finish(a, y - 40)


def shutdown(pal):
	a = start(pal, "Graceful shutdown: stop() on three pollers", "No new batches after stop(); started ones finish until shutdown-timeout; no thread is interrupted")
	x0, bar = 200, 24
	t_stop, t_out = x0 + 160, x0 + 440
	top = 112
	a.t(L, top + 16, "state", size=13, color="ink", weight="500")
	for x1, x2, label in ((x0, t_stop, "RUNNING"), (t_stop, t_out, "STOPPING"), (t_out, R, "STOPPED")):
		a.rect(x1 + 1, top, x2 - x1 - 2, bar, "card", r=2)
		a.t(x1 + 12, top + 16, label, size=CAPTION, color="soft", mono=True)
	rows = [
		("poller 1", "was pausing", [("pause", x0, t_stop)], "pause cancelled at once"),
		("poller 2", "batch in progress", [("blue", x0 + 60, t_stop + 100)], "delivered, done"),
		("poller 3", "long batch", [("blue", x0 + 100, t_out)], ""),
	]
	y = top + bar + 40
	for name, note, parts, outcome in rows:
		a.t(L, y + 10, name, size=13, color="ink", weight="600")
		a.t(L, y + 28, note)
		for hue, x1, x2 in parts:
			if hue == "pause":
				a.rect(x1, y + bar / 2 - 1, x2 - x1, 2, "bar", r=1)
			else:
				a.rect(x1, y, x2 - x1, bar, hue, r=2)
		end = parts[-1][2]
		if outcome:
			a.t(end + 12, y + 16, outcome, color="soft")
		y += bar + 40
	py = y - 40 - bar
	a.rect(t_out + 1, py + bar / 2 - 4, R - t_out - 1, 8, "amber", r=2)
	a.t(t_out + 12, py + bar + 20, "records stay PROCESSING, a WARN is logged", size=12, color="ink")
	a.t(t_out + 12, py + bar + 38, "a background job returns them by staleness", size=12)
	for x, label in ((t_stop, "stop()"), (t_out, "shutdown-timeout")):
		a.rect(x, top - 8, 1.5, py + bar + 8 - top + 8, "ink", r=0)
	a.t(t_stop + 8, top - 14, "stop()", size=CAPTION, color="ink", mono=True)
	a.t(t_out + 8, top - 14, "shutdown-timeout", size=CAPTION, color="ink", mono=True)
	return finish(a, py + bar + 38)


def duties(pal):
	a = start(pal, "One task, one abstraction", "On the left, what has to be decided; on the right, who decides it and nothing else")
	ox = 640
	rows = [
		("which records to take and how to deliver them", "take a batch, send it, save the outcome", "blue", "OutboxDispatcher"),
		("when to poll the database again", "return a pause from the outcome of a batch", "blue", "OutboxPollingStrategy"),
		("run batches one after another", "batch, pause, batch; a failed batch does not stop the loop", "blue", "OutboxPoller"),
		("how many pollers to keep", "from concurrency to max-concurrency", "blue", "OutboxPollingContainer"),
		("graceful shutdown", "cancel pauses, wait for batches, no longer than the timeout", "blue", "OutboxPollingContainer"),
		("which threads run it all", "the pause waits in a queue, not in a thread", "blue", "ScheduledExecutorService"),
		("speed up delivery after a commit", "wake a pausing poller, at most once per cooldown", "blue", "OutboxPollingContainer"),
		("the lifecycle of Spring", "start with the context, stop when it closes", "green", "OutboxPollingContainerLifecycle"),
	]
	y = 112
	for task, detail, hue, owner in rows:
		a.t(L, y + 18, task, size=13, color="ink", weight="600")
		a.t(L, y + 36, detail)
		w = b.mono_w(owner, 12) if hasattr(b, "mono_w") else len(owner) * 12 * 0.6
		w += 24
		a.rect(ox, y + 4, w, 32, hue + "-plate", r=6)
		a.t(ox + 12, y + 24.5, owner, size=12, color=b.ink(hue), weight="600", mono=True)
		y += 40 + 16
	return finish(a, y - 16)


def msg(a, xs, i, j, y, label, mono=True, near=None):
	x1, x2 = xs[i], xs[j]
	d = 1 if x2 > x1 else -1
	x1, x2 = x1 + d * 6, x2 - d * 6
	a.path(f"M{x1},{y} L{x2 - d * 6},{y}", color="line", width=1.5)
	a.add(f'<path d="M{x2},{y} L{x2 - d * 7},{y - 4} L{x2 - d * 7},{y + 4} Z" fill="{a.p["line"]}"/>')
	if label and near is None:
		a.t((x1 + x2) / 2, y - 8, label, size=CAPTION, color="soft", anchor="middle", mono=mono)
	elif label:
		a.t(xs[near] - 14, y - 8, label, size=CAPTION, color="soft", anchor="end", mono=mono)


def flow(pal):
	a = start(pal, "Polling and wake-up: who calls whom", "Time runs down. A bar is an object at work, a thin line one that waits")
	gutter = 160
	widths = [104, 96, 168, 124, 168, 140]
	lanes = [("service", "application", False), ("outbox", "table", True), ("OutboxPollingContainer", "container", True),
		("OutboxPoller", "polling loop", True), ("OutboxPollingStrategy", "pause", True), ("OutboxDispatcher", "one batch", True)]
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

	def busy(i, y1, y2):
		a.rect(xs[i] - 4, y1, 8, y2 - y1, "blue", r=2)

	y = top + hh + 44
	section(y, "start", "on startup")
	msg(a, xs, 2, 3, y, "start: concurrency pollers", mono=False)

	y += 64
	section(y, "batch", "one at a time")
	busy(3, y - 4, y + 264)
	busy(5, y - 4, y + 148)
	msg(a, xs, 3, 5, y, "dispatch()")
	msg(a, xs, 5, 1, y + 36, "findAndModifyScheduled(100)", near=5)
	msg(a, xs, 1, 5, y + 72, "up to 100 records, now PROCESSING", mono=False, near=5)
	msg(a, xs, 5, 1, y + 108, "OutboxDeliverer.send, outcome to outbox", mono=False, near=5)
	msg(a, xs, 5, 3, y + 144, "n = 100")

	y += 188
	section(y, "pause", "by the strategy")
	msg(a, xs, 3, 4, y, "calculateDelay()")
	msg(a, xs, 4, 3, y + 36, "delay = 1 s", mono=False)
	msg(a, xs, 3, 2, y + 72, "schedule(this, delay)")
	a.t(xs[3] + 10, y + 112, "waits, the thread is free", size=12)

	y += 160
	section(y, "wake-up", "optional")
	msg(a, xs, 0, 1, y, "create + commit", mono=False)
	msg(a, xs, 0, 2, y + 36, "wakeup()")
	msg(a, xs, 2, 3, y + 72, "pause cut short", mono=False)
	busy(3, y + 104, y + 132)
	msg(a, xs, 3, 5, y + 108, "dispatch()")

	y += 172
	section(y, "1000 commits", "signals merge")
	for k in range(4):
		msg(a, xs, 0, 2, y + k * 10, "1000 × wakeup()" if k == 0 else "")
	msg(a, xs, 2, 3, y + 72, "one wake-up per cooldown", mono=False)
	bottom = y + 88

	for cx in xs:
		a.parts.insert(0, f'<rect x="{cx - 0.5}" y="{top + hh}" width="1" height="{bottom - top - hh}" fill="{a.p["slot"]}"/>')
	return finish(a, bottom)


def signals(pal):
	a = start(pal, "Wake-up under load", "Signals merge: at most one database query per cooldown. Only full batches add pollers")
	x0, seconds = 200, 8
	px = (R - x0) / seconds

	def tx(t):
		return x0 + t * px

	commits = [1.5] + [2.9 + k * 0.035 for k in range(64)] + [5.4 + k * 0.02 for k in range(130)]
	woken = [1.5] + [2.9 + k * 0.1 for k in range(23)]
	idle = [5.4 + k * 0.1 for k in range(26)]
	polls = [0, 1.0, 1.5, 2.5] + woken[1:]
	top, tick, pitch = 120, 24, 64
	rows = [("commit", "in the service"), ("wakeup()", "wakes a poller"), ("poll", "a database query")]
	for index, (name, note) in enumerate(rows):
		y = top + index * pitch
		a.t(L, y + 10, name, size=13, color="ink", weight="600", mono=index == 1)
		a.t(L, y + 28, note)
		a.rect(x0, y + tick, R - x0, 1, "slot", r=0)
	for t in commits:
		a.rect(tx(t), top + 6, 1, tick - 6, "soft", r=0)
	for t in woken:
		a.rect(tx(t) - 0.75, top + pitch, 1.5, tick, "green", r=0)
	for t in idle:
		a.rect(tx(t) - 0.75, top + pitch + 8, 1.5, tick - 8, "bar", r=0)
	for t in polls:
		a.rect(tx(t) - 1, top + 2 * pitch, 2, tick, "blue", r=0)
	a.rect(tx(5.4), top + 2 * pitch, R - tx(5.4), tick, "blue", r=2)
	bottom_rows = top + 2 * pitch + tick
	for t in (2.7, 5.25):
		a.rect(tx(t), top - 8, 1, bottom_rows - top + 16, "line", r=0)
	axis = bottom_rows + 24
	for s in range(0, seconds + 1, 2):
		a.t(tx(s), axis, f"{s} s", size=AXIS, color="mute", anchor="start" if s == 0 else ("end" if s == seconds else "middle"))
	notes = [
		(0, "quiet", "a commit wakes a poller at once,", "the timer polls once a second"),
		(2.9, "a stream of commits", "a poll at most once", "per cooldown: 100 ms"),
		(5.4, "full batches", "no pauses, so the signal", "is not needed and skipped"),
	]
	y = axis + 36
	for t, head, n1, n2 in notes:
		a.t(tx(t), y, head, size=13, color="ink", weight="600")
		a.t(tx(t), y + 20, n1, size=12)
		a.t(tx(t), y + 38, n2, size=12)
	return finish(a, y + 38)



if __name__ == "__main__":
	b.OUT.mkdir(parents=True, exist_ok=True)
	drawings = (("duties", duties), ("abstractions", abstractions), ("poller", loop), ("strategies", strategies), ("scaling", scaling), ("parallel", parallel), ("failures", failures), ("wiring", wiring), ("extensions", extensions), ("shutdown", shutdown), ("flow", flow), ("signals", signals))
	for theme, pal in b.PALETTE.items():
		for name, draw in drawings:
			(b.OUT / f"delivery-polling-{name}-{theme}.svg").write_text(draw(pal).svg())
	print(f"written: {2 * len(drawings)} files")
