package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

class BoundedCommandHandler<C : Command> : CommandHandler<C> {

    override fun handle(command: C) = Unit
}
