package pro.vlprojects.semantic.ddd.spring.data.jpa.integration

import org.testcontainers.postgresql.PostgreSQLContainer
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceConfiguration

object Database {

    private val postgres = PostgreSQLContainer("postgres:16.3")
        .withReuse(true)
        .apply { start() }

    private val entityManagerFactory = PersistenceConfiguration("semantic-ddd")
        .mappingFile("META-INF/semantic-ddd-orm.xml")
        .managedClass(Cart::class.java)
        .managedClass(Item::class.java)
        .property(PersistenceConfiguration.JDBC_URL, postgres.jdbcUrl)
        .property(PersistenceConfiguration.JDBC_USER, postgres.username)
        .property(PersistenceConfiguration.JDBC_PASSWORD, postgres.password)
        .property(PersistenceConfiguration.SCHEMAGEN_DATABASE_ACTION, "drop-and-create")
        .createEntityManagerFactory()

    fun <T> runInTransaction(work: EntityManager.() -> T): T = entityManagerFactory.callInTransaction(work)
}
