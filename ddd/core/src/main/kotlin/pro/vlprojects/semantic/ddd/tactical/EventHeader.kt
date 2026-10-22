package pro.vlprojects.semantic.ddd.tactical

import java.time.Instant

/**
 * What makes an [Event] a fact of its own, apart from what it says: which one it is, of which kind and when it
 * happened. Set once, when the event is raised, so that an event read back from storage keeps its own header rather
 * than getting a new one.
 */
public interface EventHeader {
    /** Unique across all events, so that a receiver can drop a second delivery of the same one. */
    public val eventId: EventId

    /**
     * The kind of the event, such as `cart.item-added`: the same for every event of one class and kept when the class
     * is renamed, so that receivers go on telling the events apart.
     */
    public val name: String

    public val occurredAt: Instant
}
