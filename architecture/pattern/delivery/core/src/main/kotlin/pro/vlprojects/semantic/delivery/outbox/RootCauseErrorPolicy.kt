package pro.vlprojects.semantic.delivery.outbox

public data object RootCauseErrorPolicy : ErrorPolicy {

    override fun extract(cause: Throwable): String = generateSequence(cause, Throwable::cause)
        .last()
        .toString()
}
