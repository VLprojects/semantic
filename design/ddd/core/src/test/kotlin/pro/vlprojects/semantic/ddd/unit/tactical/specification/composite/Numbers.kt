package pro.vlprojects.semantic.ddd.unit.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

data object Positive : Specification<Int> {

    override fun isSatisfiedBy(candidate: Int): Boolean = candidate > 0
}

data object Even : Specification<Int> {

    override fun isSatisfiedBy(candidate: Int): Boolean = candidate % 2 == 0
}
