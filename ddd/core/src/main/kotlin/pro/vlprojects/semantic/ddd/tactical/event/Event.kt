package pro.vlprojects.semantic.ddd.tactical.event

/**
 * Something that happened in the domain, told to whoever cares, in this bounded context or another.
 *
 * An event is the domain data only. Its id, type, time and the identifier of its aggregate are metadata: an adapter
 * stamps them on an envelope when it publishes the event, so a domain class does not carry them and a deserialized
 * event does not lose them.
 *
 * An event is not typed by its aggregate, so it can live in a module of its own, apart from the model. Whatever a
 * receiver needs, the identifier of the aggregate included, the event carries in fields of its own.
 */
public interface Event
