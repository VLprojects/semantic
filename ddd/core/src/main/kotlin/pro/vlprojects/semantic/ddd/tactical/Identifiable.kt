package pro.vlprojects.semantic.ddd.tactical

public interface Identifiable<out I : Identifier> {
    // Not `id`: it would clash with `Persistable.getId()` of Spring Data (KT-6653).
    public val identifier: I
}
