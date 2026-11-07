package pro.vlprojects.semantic.ddd.spring.data.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.NoRepositoryBean
import pro.vlprojects.semantic.ddd.architecture.error.NotFoundException
import pro.vlprojects.semantic.ddd.spring.data.repository.EntityRepository
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier
import jakarta.persistence.LockModeType

@NoRepositoryBean
public interface JpaEntityRepository<E : Entity<E, I>, I : Identifier> :
    JpaRepository<E, I>,
    EntityRepository<E, I> {

    // Spring Data derives the query from the name; findByIdForUpdate would look for a property idForUpdate.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public fun findForUpdateById(identifier: I): E?

    // A default body keeps Spring Data from deriving a query out of the name.
    public fun findForUpdateByIdOrThrow(identifier: I): E = findForUpdateById(identifier)
        ?: throw NotFoundException("Entity not found [id: $identifier]")
}
