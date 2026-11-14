package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

open class CommandModule<C> {

    open inner class Handlers {

        open inner class Handler : CommandHandler<C> {

            override fun handle(command: C) = Unit
        }
    }
}
