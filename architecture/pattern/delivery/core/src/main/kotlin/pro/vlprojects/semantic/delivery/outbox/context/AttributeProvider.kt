package pro.vlprojects.semantic.delivery.outbox.context

public fun interface AttributeProvider : ContextProvider {

    public fun provideAttributes(): Map<String, String>

    override fun provide(payload: Any, builder: Context.Builder) {
        builder.addAllAttributes(provideAttributes())
    }
}
