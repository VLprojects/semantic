package pro.vlprojects.semantic.ddd.architecture.error

// Unchecked: Spring rolls a transaction back on a RuntimeException only.
public abstract class DomainException(message: String) : RuntimeException(message)

public open class NotFoundException(message: String) : DomainException(message)

public open class ConflictException(message: String) : DomainException(message)

public open class BusinessRuleException(message: String) : DomainException(message)
