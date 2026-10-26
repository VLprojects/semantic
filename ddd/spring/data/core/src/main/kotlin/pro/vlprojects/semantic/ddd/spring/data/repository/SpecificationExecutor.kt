package pro.vlprojects.semantic.ddd.spring.data.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import pro.vlprojects.semantic.ddd.tactical.Specification

public interface SpecificationExecutor<T : Any> {

    public fun findOne(specification: Specification<T>): T?

    public fun findOneOrThrow(specification: Specification<T>): T

    public fun findAll(specification: Specification<T>): List<T>

    public fun findAll(specification: Specification<T>, sort: Sort): List<T>

    public fun findAll(specification: Specification<T>, pageable: Pageable): Page<T>

    public fun count(specification: Specification<T>): Long

    public fun exists(specification: Specification<T>): Boolean
}
