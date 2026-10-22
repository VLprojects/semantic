package pro.vlprojects.semantic.ddd.error

/**
 * A refusal of the domain: a rule does not hold, the subject does not exist, or one like it already does.
 *
 * Unchecked on purpose. Spring rolls a transaction back on a `RuntimeException` and commits it on a checked one, so
 * a checked refusal would leave half of a change in the database. Nor is there a reason for it to be checked: the
 * domain cannot handle its own refusal, it only carries it to the edge of the application.
 *
 * The set is closed: each subclass is an answer an adapter knows how to give. A new kind of refusal is a decision made
 * here, not a word a service happened to miss.
 */
public sealed class DomainException(message: String) : RuntimeException(message)

/** What the request refers to does not exist. */
public class NotFoundException(message: String) : DomainException(message)

/** One like it already exists, and there cannot be a second. */
public class ConflictException(message: String) : DomainException(message)

/** A business rule does not hold: what is asked can be done, but not in this state. */
public class BusinessRuleException(message: String) : DomainException(message)
