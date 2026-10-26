package pro.vlprojects.semantic.ddd.spring.data.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.tactical.Specification
import java.util.stream.Stream

public interface SpecificationExecutor<T : Any> {

    public fun <R> findBy(specification: Specification<T>, query: (FetchableFluentQuery<T>) -> R): R

    public fun findOne(specification: Specification<T>): T? = findBy(specification) { it.oneValue() }

    public fun findOneOrThrow(specification: Specification<T>): T = findOne(specification)
        ?: throw NotFoundException("Entity not found [specification: $specification]")

    public fun findAll(specification: Specification<T>, sort: Sort = Sort.unsorted()): List<T> =
        findBy(specification) { it.sortBy(sort).all() }

    public fun findAllPaged(specification: Specification<T>, pageable: Pageable): Page<T> =
        findBy(specification) { it.page(pageable) }

    public fun findAllSliced(specification: Specification<T>, pageable: Pageable): Slice<T> =
        findBy(specification) { it.slice(pageable) }

    public fun findAllStreamed(specification: Specification<T>, sort: Sort = Sort.unsorted()): Stream<T> =
        findBy(specification) { it.sortBy(sort).stream() }

    public fun findAllScrolled(
        specification: Specification<T>,
        position: ScrollPosition,
        sort: Sort = Sort.unsorted(),
    ): Window<T> = findBy(specification) { it.sortBy(sort).scroll(position) }

    public fun count(specification: Specification<T>): Long = findBy(specification) { it.count() }

    public fun exists(specification: Specification<T>): Boolean = findBy(specification) { it.exists() }
}
