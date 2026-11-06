package pro.vlprojects.semantic.ddd.strategic

import pro.vlprojects.semantic.format.toKebabCase

public sealed interface Subdomain {

    public val name: String
        get() = this::class.toKebabCase()

    public interface Core : Subdomain

    public interface Supporting : Subdomain

    public interface Generic : Subdomain
}
