package pro.vlprojects.semantic.types.version

public interface Successive<out V : Version> {

    public fun next(): V
}
