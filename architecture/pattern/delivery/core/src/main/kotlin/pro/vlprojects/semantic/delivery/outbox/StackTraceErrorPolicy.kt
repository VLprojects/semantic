package pro.vlprojects.semantic.delivery.outbox

public data object StackTraceErrorPolicy : ErrorPolicy {

    override fun extract(cause: Throwable): String = cause.stackTraceToString()
}
