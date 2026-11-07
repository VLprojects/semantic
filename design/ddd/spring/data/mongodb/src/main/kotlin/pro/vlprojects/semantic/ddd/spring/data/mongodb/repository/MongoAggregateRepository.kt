package pro.vlprojects.semantic.ddd.spring.data.mongodb.repository

import org.springframework.data.repository.NoRepositoryBean
import pro.vlprojects.semantic.ddd.spring.data.repository.AggregateRepository
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface MongoAggregateRepository<A : AggregateRoot<A, I>, I : Identifier> :
    MongoEntityRepository<A, I>,
    AggregateRepository<A, I>
