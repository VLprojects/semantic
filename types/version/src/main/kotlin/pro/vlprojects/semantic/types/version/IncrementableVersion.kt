package pro.vlprojects.semantic.types.version

public interface IncrementableVersion<V : IncrementableVersion<V>> : OrderedVersion<V> {

    public fun next(): V
}
