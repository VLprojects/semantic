package pro.vlprojects.semantic.ddd.error

/** What kind of refusal a [DomainException] is. An adapter answers by the kind, not by the class. */
public enum class ErrorKind {
    /** What the request refers to does not exist. HTTP 404. */
    NotFound,

    /** One like it already exists, and there cannot be a second. HTTP 409. */
    Conflict,

    /** What is asked can be done, but not in this state. HTTP 422. */
    BusinessRule,
}
