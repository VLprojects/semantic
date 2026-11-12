package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

interface OrderCommand<out R> {

    interface Handler<C : OrderCommand<R>, R> {

        fun handle(command: C): R
    }
}
