package pro.vlprojects.semantic.ddd.strategic

import pro.vlprojects.semantic.ddd.support.kebabClassName

public sealed interface Subdomain {

    public val name: String
        get() = kebabClassName

    public interface Core : Subdomain

    public interface Supporting : Subdomain

    public interface Generic : Subdomain
}
