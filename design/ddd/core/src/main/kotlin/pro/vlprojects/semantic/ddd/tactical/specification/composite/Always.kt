package pro.vlprojects.semantic.ddd.tactical.specification.composite

import pro.vlprojects.semantic.ddd.tactical.Specification

public data object Always : Specification<Any?> {

    override fun isSatisfiedBy(candidate: Any?): Boolean = true
}
