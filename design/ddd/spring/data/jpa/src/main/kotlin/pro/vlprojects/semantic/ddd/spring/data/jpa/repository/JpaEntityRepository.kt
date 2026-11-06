package pro.vlprojects.semantic.ddd.spring.data.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import pro.vlprojects.semantic.ddd.spring.data.repository.EntityRepository
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface JpaEntityRepository<E : Entity<E, I>, I : Identifier> :
    JpaRepository<E, I>,
    EntityRepository<E, I>
