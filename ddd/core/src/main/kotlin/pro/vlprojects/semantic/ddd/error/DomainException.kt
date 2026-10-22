package pro.vlprojects.semantic.ddd.error

/**
 * A refusal of the domain: a rule does not hold, the subject does not exist, or one like it already does.
 *
 * Unchecked on purpose. Spring rolls a transaction back on a `RuntimeException` and commits it on a checked one, so
 * a checked refusal would leave half of a change in the database. Nor is there a reason for it to be checked: the
 * domain cannot handle its own refusal, it only carries it to the edge of the application.
 *
 * Open: a service subclasses the refusals below for the cases it tells apart, or this class with the [kind] an adapter
 * should answer with.
 */
public abstract class DomainException(
    public val kind: ErrorKind,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/** What the request refers to does not exist. */
public open class NotFoundException(message: String) : DomainException(ErrorKind.NotFound, message)

/** One like it already exists, and there cannot be a second. */
public open class ConflictException(message: String) : DomainException(ErrorKind.Conflict, message)

/** A business rule does not hold: what is asked can be done, but not in this state. */
public open class BusinessRuleException(message: String) : DomainException(ErrorKind.BusinessRule, message)
