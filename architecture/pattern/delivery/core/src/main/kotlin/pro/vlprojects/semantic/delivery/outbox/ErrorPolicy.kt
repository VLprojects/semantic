package pro.vlprojects.semantic.delivery.outbox

public fun interface ErrorPolicy {

    public fun extract(cause: Throwable): String
}
