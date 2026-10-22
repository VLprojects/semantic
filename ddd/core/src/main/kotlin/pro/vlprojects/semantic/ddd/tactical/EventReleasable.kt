package pro.vlprojects.semantic.ddd.tactical

/**
 * An aggregate of type [A] that hands out the events of its changes once they are saved; how it records them stays
 * inside the implementation.
 */
public interface EventReleasable<A : AggregateRoot<A, *>> {
    /**
     * Hands the recorded events out, in the order recorded, and forgets them in one step, so that none is forgotten
     * before it is handed out.
     */
    public fun releaseEvents(): Collection<Event<A, *>>
}
