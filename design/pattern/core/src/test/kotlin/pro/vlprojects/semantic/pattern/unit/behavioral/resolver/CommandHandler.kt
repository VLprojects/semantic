package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

fun interface CommandHandler<C> {

    fun handle(command: C)
}
