package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.postgresql.PostgreSQLContainer
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceConfiguration

object Database {

    private val postgres = PostgreSQLContainer("postgres:16.3")
        .withReuse(true)
        .apply { start() }

    private val dataSource = PGSimpleDataSource()
        .apply {
            setURL(postgres.jdbcUrl)
            user = postgres.username
            password = postgres.password
        }
        .let(ProxyDataSourceBuilder::create)
        .countQuery()
        .build()

    private val entityManagerFactory = PersistenceConfiguration("semantic-ddd")
        .managedClass(Cart::class.java)
        .managedClass(Item::class.java)
        .property("jakarta.persistence.nonJtaDataSource", dataSource)
        .property(PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION, "drop-and-create")
        .createEntityManagerFactory()

    fun <T> runInTransaction(work: EntityManager.() -> T): T = entityManagerFactory.callInTransaction(work)
}
