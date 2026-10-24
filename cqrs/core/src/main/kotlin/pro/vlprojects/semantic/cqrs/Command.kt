package pro.vlprojects.semantic.cqrs

public interface Command<out R> {

    public interface Bus {

        public fun <R> dispatch(command: Command<R>): R
    }

    public interface Handler<C : Command<R>, R> {

        public fun handle(command: C): R
    }
}
