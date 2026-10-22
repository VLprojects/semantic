package pro.vlprojects.semantic.ddd.identity

/**
 * Identity of an entity. Each entity type gets an identifier type of its own, so an order id cannot be passed where a
 * customer id is expected.
 *
 * A key of one value implements [Id]; a composite key implements this interface directly.
 */
public interface Identifier
