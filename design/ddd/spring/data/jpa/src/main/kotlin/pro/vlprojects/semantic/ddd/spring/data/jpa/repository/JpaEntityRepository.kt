package pro.vlprojects.semantic.ddd.spring.data.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
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

    // A derived query would split the name at Or; Hibernate reads id as the identifier whatever the attribute is named.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from #{#entityName} e where e.id = ?1")
    public fun findForUpdateByIdOrNull(identifier: I): E?

    // A default body keeps Spring Data from deriving a query out of the name.
    public fun findForUpdateByIdOrThrow(identifier: I): E = findForUpdateByIdOrNull(identifier)
        ?: throw NotFoundException("Entity not found [id: $identifier]")
}
