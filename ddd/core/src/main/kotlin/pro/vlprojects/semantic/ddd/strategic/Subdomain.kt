package pro.vlprojects.semantic.ddd.strategic

public sealed interface Subdomain {

    public val name: String
        get() = kebabClassName

    public interface Core : Subdomain

    public interface Supporting : Subdomain

    public interface Generic : Subdomain
}
