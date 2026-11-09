package pro.vlprojects.semantic.delivery.outbox.context

public fun interface AttributeProvider : ContextProvider {

    public fun provideAttributes(): Map<String, String>

    override fun provide(payload: Any, context: Context): Context = provideAttributes()
        .let(context::withDefaultAttributes)
}
