package pro.vlprojects.semantic.ddd.spring.data.repository

import org.springframework.data.repository.NoRepositoryBean
import pro.vlprojects.semantic.ddd.tactical.AggregateRoot
import pro.vlprojects.semantic.ddd.tactical.Identifier

@NoRepositoryBean
public interface AggregateRepository<A : AggregateRoot<A, I>, I : Identifier> : EntityRepository<A, I>
