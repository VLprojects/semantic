package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

interface ResultCommand<out R> {

    interface Handler<C : ResultCommand<R>, R> {

        fun handle(command: C): R
    }
}
