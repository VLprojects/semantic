package pro.vlprojects.semantic.ddd.tactical

/**
 * Records events while it changes and hands them out once the change is saved. Usually an [AggregateRoot]; what
 * records them, and how, stays inside the implementation.
 *
 * The events are not typed by the recorder: only the recorder itself records them, so only its own events get in,
 * and whoever releases them, an adapter publishing them, does not need their type.
 */
public interface EventRecorder {
    /**
     * Hands the recorded events out, in the order recorded, and forgets them in one step, so that none is forgotten
     * before it is handed out.
     */
    public fun releaseEvents(): Collection<Event>
}
