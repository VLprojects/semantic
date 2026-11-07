package pro.vlprojects.semantic.ddd.spring.data.mongodb.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.NoRepositoryBean
import pro.vlprojects.semantic.ddd.spring.data.repository.EntityRepository
import pro.vlprojects.semantic.ddd.tactical.Entity
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface MongoEntityRepository<E : Entity<E, I>, I : Identifier> :
    MongoRepository<E, I>,
    EntityRepository<E, I>
