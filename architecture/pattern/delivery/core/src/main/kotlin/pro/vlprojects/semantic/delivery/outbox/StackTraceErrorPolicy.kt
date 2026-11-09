package pro.vlprojects.semantic.delivery.outbox

public class StackTraceErrorPolicy : ErrorPolicy {

    override fun extract(cause: Throwable): String = cause.stackTraceToString()
}
