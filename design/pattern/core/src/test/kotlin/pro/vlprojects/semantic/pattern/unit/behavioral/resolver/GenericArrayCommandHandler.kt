package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class GenericArrayCommandHandler<C> : ArrayCommandHandler<C>() {

    override fun handle(command: Array<C>) = Unit
}
