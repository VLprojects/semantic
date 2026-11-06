package pro.vlprojects.semantic.ddd.strategic

public enum class Complexity(public val businessLogicPattern: BusinessLogicPattern) {

    LOW(BusinessLogicPattern.TRANSACTION_SCRIPT),
    HIGH(BusinessLogicPattern.DOMAIN_MODEL),
}
