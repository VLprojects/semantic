package pro.vlprojects.semantic.delivery.outbox.context

public fun interface AttributeProvider {

    public fun provide(): Map<String, String>
}
