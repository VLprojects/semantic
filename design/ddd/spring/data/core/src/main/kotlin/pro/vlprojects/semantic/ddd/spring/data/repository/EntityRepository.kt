package pro.vlprojects.semantic.ddd.spring.data.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.ListPagingAndSortingRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface EntityRepository<E : Entity<E, I>, I : Identifier> :
    ListCrudRepository<E, I>,
    ListPagingAndSortingRepository<E, I> {

    // A default body keeps Spring Data from deriving a query out of the name.
    public fun findByIdOrThrow(id: I): E = findByIdOrNull(id)
        ?: throw NotFoundException("Entity not found [id: $id]")
}
