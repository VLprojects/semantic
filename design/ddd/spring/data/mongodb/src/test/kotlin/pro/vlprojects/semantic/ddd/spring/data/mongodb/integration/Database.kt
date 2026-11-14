package pro.vlprojects.semantic.ddd.spring.data.mongodb.integration

import com.mongodb.client.MongoClients
import org.springframework.data.mapping.callback.EntityCallbacks
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.testcontainers.mongodb.MongoDBContainer
import pro.vlprojects.semantic.ddd.spring.data.mongodb.PersistedMarkingCallback

object Database {

    private val mongo = MongoDBContainer("mongo:7.0.34")
        .withReuse(true)
        .apply { start() }

    val template = mongo.connectionString
        .let(MongoClients::create)
        .let { SimpleMongoClientDatabaseFactory(it, "semantic-ddd") }
        .let(::MongoTemplate)
        .apply { PersistedMarkingCallback().let(EntityCallbacks::create).let(::setEntityCallbacks) }
        // The container outlives a run, and the keys of the tests start over.
        .apply { db.drop() }

    val mappingContext = template.converter.mappingContext
}
