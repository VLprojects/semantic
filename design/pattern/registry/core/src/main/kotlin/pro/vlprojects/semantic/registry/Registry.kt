package pro.vlprojects.semantic.registry

public interface Registry<T : Any> {

    public val names: Set<String>

    public fun findByName(name: String): T?
}
