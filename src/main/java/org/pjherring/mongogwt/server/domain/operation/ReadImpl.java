/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import org.pjherring.mongogwt.server.domain.translate.DBObjectToPojo;
import org.pjherring.mongogwt.server.domain.translate.PojoFlushOut;
import org.pjherring.mongogwt.shared.domain.operation.Read;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.exception.InvalidReference;
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
    protected PojoFlushOut flushOut;

    @Inject
    public ReadImpl(DB mongoDb, DBObjectToPojo translator, PojoFlushOut flushOut) {
        this.mongoDb = mongoDb;
        this.translator = translator;
        this.flushOut = flushOut;
    }

    /*
     * Finds a set of Entities based on a query.
     *
     * @param query The query to use.
     * @param clazz The type of entities to return.
     * @param doFanOut Fetch all the references as complete entities or just
     *      with their id's and created datetimes set.
     *
     * @return results
     */
    public <T extends IsEntity> List<T> find(
        Query query,
        Class<T> clazz,
        boolean doFanOut) {

        DBCursor cursor = doQuery(query, clazz);

        //do we have results?
        if (cursor.size() > 0) {
            List<T> results = new ArrayList<T>();

            //iterate through and translate
            while (cursor.hasNext()) {

                DBObject current = cursor.next();
                T foundPojo = translator.translate(current, clazz, doFanOut);
                flushOut.flush(foundPojo, doFanOut);
                results.add(foundPojo);
            }

            return results;
        }

        //only gets here if we had no results
        throw new NotFoundException("No resulsts!");
    }

    /*
     * Same as @see find but just finds one object
     *
     * @param query The query to use.
     * @param clazz The type of entities to return.
     * @param doFanOut Fetch all the references as complete entities or just
     *      with their id's and created datetimes set.
     *
     * @return the found entity
     */
    public <T extends IsEntity> T findOne(
        Query query,
        Class<T> clazz,
        boolean doFanOut) {

        DBCursor cursor = doQuery(query.setLimit(1), clazz);

        if (cursor.size() > 0) {
            T foundPojo = translator.translate(cursor.next(), clazz, doFanOut);
            flushOut.flush(foundPojo, doFanOut);
            return foundPojo;
        }

        throw new NotFoundException("No resulsts!");
    }

    /*
     * Same as @see findOne just uses the id to find the object.
     *
     * @param id Id of the object to find.
     * @param clazz The type of entities to return.
     * @param doFanOut Fetch all the references as complete entities or just
     *      with their id's and created datetimes set.
     *
     * @return the found entity
     */
    public <T extends IsEntity> T findById(
        String id,
        Class<T> clazz,
        boolean doFanOut) {

        DBCursor cursor = doQuery(
            new Query().start("_id").is(new ObjectId(id)).setLimit(1),
            clazz
        );

        if (cursor.size() > 0) {
            DBObject dbObject = cursor.next();
            return translator.translate(dbObject, clazz, doFanOut);
        }

        throw new NotFoundException("No results!");
    }

    private DBCursor doQuery(Query query, Class<? extends IsEntity> clazz) {
        doQueryManipulation(query);

        //transform our map into a Mongo DBObject
        DBObject queryAsDBObject
            = BasicDBObjectBuilder.start(query.getQueryMap()).get();

        DBCursor cursor =
            mongoDb.getCollection(clazz.getAnnotation(Entity.class).name())
                .find(queryAsDBObject);

        if (!query.getSortMap().isEmpty()) {
            cursor.sort(new BasicDBObject(query.getSortMap()));
        }

        if (query.getLimit() > -1) {
            cursor.limit(query.getLimit());
        }

        return cursor;
    }

    private void doQueryManipulation(Query query) {

        //iterate through looking for references and regular expressions
        for (String key : query.getQueryMap().keySet()) {

            Object value = query.getQueryMap().get(key);

            if (value instanceof Query.Reference) {
                Query.Reference reference = (Query.Reference) value;

                if (!IsEntity.class.isAssignableFrom(reference.getEntityReference())
                    || !reference.getEntityReference().isAnnotationPresent(Entity.class)) {
                    throw new InvalidReference(reference.getEntityReference().getName());
                }
                Class<? extends IsEntity> referencedClass =
                    (Class<? extends IsEntity>) reference.getEntityReference();
                String collectionName = 
                    referencedClass.getAnnotation(Entity.class).name();
                value = new DBRef(
                    mongoDb,
                    collectionName, 
                    new ObjectId(reference.getId())
                );
                query.getQueryMap().put(key, value);
            } else if (query.getRegularExpressionKeys().contains(key)) {
                /*
                 * Regular expression
                 */
                query.getQueryMap().put(key, Pattern.compile((String) value));
            }
        }
    }

}
