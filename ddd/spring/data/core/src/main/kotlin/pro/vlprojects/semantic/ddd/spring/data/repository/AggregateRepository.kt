package pro.vlprojects.semantic.ddd.spring.data.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.ListPagingAndSortingRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface AggregateRepository<A : AggregateRoot<A, I>, I : Identifier> :
    ListCrudRepository<A, I>,
    ListPagingAndSortingRepository<A, I> {

    // A default body keeps Spring Data from deriving a query out of the name.
    public fun findByIdOrThrow(identifier: I): A = findByIdOrNull(identifier)
        ?: throw NotFoundException("No aggregate with identifier $identifier")
}
