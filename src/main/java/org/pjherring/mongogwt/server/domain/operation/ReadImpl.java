/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class ReadImpl implements Read {

    private final static Logger LOG = Logger.getLogger(ReadImpl.class.getName());
    protected DB mongoDb;
    protected DBObjectToPojo translator;

    @Inject
    public ReadImpl(DB mongoDb, DBObjectToPojo translator) {
        this.mongoDb = mongoDb;
        this.translator = translator;
    }

    public <T extends IsEntity> List<T> find(Query query, Class<T> clazz, boolean doFanOut) {
        DBObject queryAsDBObject 
            = BasicDBObjectBuilder.start(query.getQueryMap()).get();

        DBCursor cursor;
        if (query.getSortMap().isEmpty()) {
            cursor =
                mongoDb.getCollection(clazz.getAnnotation(Entity.class).name())
                    .find(queryAsDBObject);
        } else {
            cursor =
                mongoDb.getCollection(clazz.getAnnotation(Entity.class).name())
                    .find(queryAsDBObject)
                    .sort(new BasicDBObject(query.getSortMap()));
        }

        if (query.getLimit() > -1) {
            cursor.limit(query.getLimit());
        }


        if (cursor.size() > 0) {
            List<T> results = new ArrayList<T>();

            while (cursor.hasNext()) {
                DBObject current = cursor.next();
                results.add(translator.translate(current, clazz));
            }

            return results;
        }

        throw new NotFoundException("No resulsts!");
    }

    public <T extends IsEntity> T findOne(Query query, Class<T> clazz, boolean doFanOut) {
        DBObject queryAsDBObject
            = BasicDBObjectBuilder.start(query.getQueryMap()).get();

        DBCursor cursor;
        if (query.getSortMap().isEmpty()) {
            cursor =
                mongoDb.getCollection(clazz.getAnnotation(Entity.class).name())
                    .find(queryAsDBObject);
        } else {
            cursor =
                mongoDb.getCollection(clazz.getAnnotation(Entity.class).name())
                    .find(queryAsDBObject)
                    .sort(new BasicDBObject(query.getSortMap()));
        }

        cursor.limit(1);


        if (cursor.size() > 0) {
            return translator.translate(cursor.next(), clazz);
        }

        throw new NotFoundException("No resulsts!");
    }

    public <T extends IsEntity> T findById(String id, Class<T> clazz, boolean doFanOut) {

        DBCursor cursor =
            mongoDb.getCollection(clazz.getAnnotation(Entity.class).name())
            .find(new BasicDBObject("_id", new ObjectId(id)));

        cursor.limit(1);

        if (cursor.size() > 0) {
            return translator.translate(cursor.next(), clazz);
        }

        throw new NotFoundException("No results!");
    }

}
