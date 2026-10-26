package pro.vlprojects.semantic.cqrs

public interface Query<out R> {

    public interface Bus {

        public fun <R> ask(query: Query<R>): R
    }

    public interface Handler<Q : Query<R>, R> {

        public fun handle(query: Q): R
    }
}
