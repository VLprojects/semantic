package pro.vlprojects.semantic.pattern.behavioral.visitor

/** The base type of the visitors of a hierarchy: its visit methods call [visit] unless a visitor overrides them. */
public interface Visitor<in E, out R> {

    public fun visit(element: E): R
}
