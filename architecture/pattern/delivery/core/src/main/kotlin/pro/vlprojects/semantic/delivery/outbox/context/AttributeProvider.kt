package pro.vlprojects.semantic.delivery.outbox.context

public fun interface AttributeProvider : ContextProvider {

    public fun provideAttributes(): Map<String, String>

    override fun enrich(payload: Any, builder: Context.Builder) {
        provideAttributes().let(builder::addAllAttributes)
    }
}
