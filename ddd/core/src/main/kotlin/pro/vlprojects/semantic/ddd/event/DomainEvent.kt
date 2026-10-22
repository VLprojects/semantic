package pro.vlprojects.semantic.ddd.event

/**
 * Something that happened in the domain, told to whoever cares, in this bounded context or another.
 *
 * An event is the domain data only. Its id, type, time and the identifier of its aggregate are metadata: an adapter
 * stamps them on an envelope when it publishes the event, so a domain class does not carry them and a deserialized
 * event does not lose them.
 *
 * Which aggregate an event belongs to is told by the aggregate, not by the event: see
 * [pro.vlprojects.semantic.ddd.model.AggregateRoot]. Whatever a receiver needs, the identifier of the aggregate
 * included, the event carries in fields of its own.
 */
public interface DomainEvent
