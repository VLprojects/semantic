package pro.vlprojects.semantic.delivery.outbox.error

public fun interface ErrorPolicy {

    public fun extract(cause: Throwable): String
}
