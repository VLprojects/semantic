package pro.vlprojects.semantic.ddd.tactical

/**
 * Something whose state is saved in versions, one per saved change. Two changes made from the same version conflict:
 * the second one to be saved is refused, so that it does not silently undo the first.
 *
 * Read from outside as well: an adapter answers a stale `If-Match` with it, and an envelope of an event carries it to
 * order the events of one aggregate.
 */
public interface Versioned {
    /** The version the state was loaded at; 0 for one not saved yet. Moved forward by the storage, not by the domain. */
    public val version: Long
}
