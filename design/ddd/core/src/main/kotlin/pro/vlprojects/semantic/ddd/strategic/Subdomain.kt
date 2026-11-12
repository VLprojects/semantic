package pro.vlprojects.semantic.ddd.strategic

import pro.vlprojects.semantic.format.toKebabCase

public sealed interface Subdomain {

    public val name: String
        get() = this::class.toKebabCase()

    public val complexity: Complexity

    public val volatility: Volatility

    public val businessLogicPattern: BusinessLogicPattern
        get() = complexity.businessLogicPattern

    public val architecturalPattern: ArchitecturalPattern
        get() = businessLogicPattern.architecturalPattern

    public val testingStrategy: TestingStrategy
        get() = businessLogicPattern.testingStrategy

    public interface Core : Subdomain {

        override val complexity: Complexity
            get() = Complexity.HIGH

        override val volatility: Volatility
            get() = Volatility.HIGH
    }

    public interface Supporting : Subdomain {

        override val complexity: Complexity
            get() = Complexity.LOW

        override val volatility: Volatility
            get() = Volatility.LOW
    }

    public interface Generic : Subdomain {

        override val complexity: Complexity
            get() = Complexity.HIGH

        override val volatility: Volatility
            get() = Volatility.LOW
    }
}
