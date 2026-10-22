package pro.vlprojects.semantic.ddd.tactical

/**
 * An aggregate at one point of its history: which one it is and the version it had. An [Event] names the revision it
 * happened at, so that a receiver can tell a stale event from a missed one: an event of an older version than the
 * one it has seen is stale, a gap in versions means one is missing.
 *
 * The version is the version of the state, the one of [Versioned]: events raised in one change share it and are
 * ordered as they were raised.
 */
public interface AggregateRevision<out I : Identifier> :
    Identifiable<I>,
    Versioned
