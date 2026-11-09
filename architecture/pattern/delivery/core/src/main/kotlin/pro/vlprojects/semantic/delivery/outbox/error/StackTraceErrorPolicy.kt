package pro.vlprojects.semantic.delivery.outbox.error

public class StackTraceErrorPolicy : ErrorPolicy {

    override fun extract(cause: Throwable): String = cause.stackTraceToString()
}
