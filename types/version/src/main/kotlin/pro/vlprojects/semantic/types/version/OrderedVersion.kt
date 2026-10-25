package pro.vlprojects.semantic.types.version

public interface OrderedVersion<V : OrderedVersion<V>> : Version, Comparable<V>
