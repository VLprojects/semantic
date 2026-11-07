package pro.vlprojects.semantic.ddd.spring.data.jpa.repository

import org.springframework.data.repository.NoRepositoryBean
import pro.vlprojects.semantic.ddd.spring.data.repository.AggregateRepository
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface JpaAggregateRepository<A : AggregateRoot<A, I>, I : Identifier> :
    JpaEntityRepository<A, I>,
    AggregateRepository<A, I>
