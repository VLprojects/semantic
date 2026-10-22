package pro.vlprojects.semantic.ddd.tactical

/**
 * Something that hands out the events of its changes once they are saved. Usually an [AggregateRoot]; how it records
 * them stays inside the implementation.
 *
 * The events are not typed: only the implementation itself records them, so only its own events get in, and whoever
 * releases them, an adapter publishing them, does not need their type.
 */
public interface EventReleasable {
    /**
     * Hands the recorded events out, in the order recorded, and forgets them in one step, so that none is forgotten
     * before it is handed out.
     */
    public fun releaseEvents(): Collection<Event>
}
