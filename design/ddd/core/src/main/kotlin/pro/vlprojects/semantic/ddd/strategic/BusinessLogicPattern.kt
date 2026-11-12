package pro.vlprojects.semantic.ddd.strategic

public enum class BusinessLogicPattern(
    public val architecturalPattern: ArchitecturalPattern,
    public val testingStrategy: TestingStrategy,
) {

    TRANSACTION_SCRIPT(ArchitecturalPattern.LAYERED, TestingStrategy.REVERSED_PYRAMID),
    ACTIVE_RECORD(ArchitecturalPattern.LAYERED, TestingStrategy.DIAMOND),
    DOMAIN_MODEL(ArchitecturalPattern.PORTS_AND_ADAPTERS, TestingStrategy.PYRAMID),
    EVENT_SOURCED_DOMAIN_MODEL(ArchitecturalPattern.CQRS, TestingStrategy.PYRAMID),
}
