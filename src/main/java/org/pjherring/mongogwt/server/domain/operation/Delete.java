/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.pjherring.mongogwt.shared.IsDomainObject;
import org.pjherring.mongogwt.shared.domain.operation.DoesDelete;
import org.pjherring.mongogwt.shared.query.Query;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.exceptions.NotFoundException;

/**
 *
 * @author pjherring
 */
public class Delete extends BaseDatabaseOperation implements DoesDelete {

    public <T extends IsDomainObject> void doDelete(Query query, Class<T> type) {
        validator.validateCollection(type);

        Entity collectionDb = type.getAnnotation(Entity.class);
        DBCollection collection = getDatabase().getCollection(collectionDb.name());
        DBObject finder = BasicDBObjectBuilder.start(query.getQueryMap()).get();

        if (collection.find(finder).length() != 0) {
            collection.remove(finder);
        } else {
            throw new NotFoundException();
        }
    }

}
