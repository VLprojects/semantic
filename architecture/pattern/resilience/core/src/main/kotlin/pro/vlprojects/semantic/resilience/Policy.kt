package pro.vlprojects.semantic.resilience

public interface Policy {

    public fun <T> execute(action: () -> T): T
}
