package pro.vlprojects.semantic.ddd.spring.data.mongodb

import org.bson.Document
import org.springframework.data.mongodb.core.mapping.event.AfterConvertCallback
import org.springframework.data.mongodb.core.mapping.event.AfterSaveCallback

public class PersistedMarkingCallback :
    AfterConvertCallback<AbstractMongoEntity<*, *>>,
    AfterSaveCallback<AbstractMongoEntity<*, *>> {

    override fun onAfterConvert(
        entity: AbstractMongoEntity<*, *>,
        document: Document,
        collection: String,
    ): AbstractMongoEntity<*, *> = entity.apply { markPersistedByStore() }

    override fun onAfterSave(
        entity: AbstractMongoEntity<*, *>,
        document: Document,
        collection: String,
    ): AbstractMongoEntity<*, *> = entity.apply { markPersistedByStore() }
}
