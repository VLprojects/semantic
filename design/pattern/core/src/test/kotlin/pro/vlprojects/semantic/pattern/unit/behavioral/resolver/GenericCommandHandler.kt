package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class GenericCommandHandler<C> : CommandHandler<C> {

    override fun handle(command: C) = Unit
}
