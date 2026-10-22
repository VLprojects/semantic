package pro.vlprojects.semantic.ddd.tactical

/**
 * Something that hands out events of type [E] once its changes are saved; how it records them stays inside the
 * implementation. An [AggregateRoot] releases its own events: `EventReleasable<Event<A>>`.
 */
public interface EventReleasable<out E : Event<*>> {
    /**
     * Hands the recorded events out, in the order recorded, and forgets them in one step, so that none is forgotten
     * before it is handed out.
     */
    public fun releaseEvents(): Collection<E>
}
