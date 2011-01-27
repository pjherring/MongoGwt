/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.domain.operation.DoesRead;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Enumerated;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.EnumeratedType;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.InvalidEntity;
import org.pjherring.mongogwt.shared.exception.InvalidReference;
import org.pjherring.mongogwt.shared.exception.NotFoundException;
import org.pjherring.mongogwt.shared.exception.QueryException;
import org.pjherring.mongogwt.shared.query.Query;

/**
 *
 * @author pjherring
 */
public class ReadBu extends BaseDatabaseOperation implements DoesRead {

    private final Logger LOG = Logger.getLogger("Read");

    protected Map<ObjectId, IsEntity> queryCache
        = new HashMap<ObjectId, IsEntity>();

    public <T extends IsEntity> List<T> find(
        Query query, Class<T> collectionClass, boolean doFanOut
    ) {
        if (query.getQueryMap().containsKey("_id")) {
            throw new Error("Can not fetch by id, use fetchOne instead.");
        }

        queryCache.clear();
        List<T> results = doQuery(query, collectionClass, doFanOut);
        if (results.size() > 0) {
            return results;
        }

        throw new NotFoundException();
    }

    public <T extends IsEntity> T findById(
        String id, Class<T> collectionClass, boolean doFanOut) {

        doCollectionClassValidation(collectionClass);
        Entity collectionDb = collectionClass.getAnnotation(Entity.class);

        DBObject fetched = getDatabase().getCollection(collectionDb.name()).findOne(new ObjectId(id));

        queryCache.clear();
        return buildDomainObject(fetched, collectionClass, doFanOut);
    }

    public <T extends IsEntity> T findOne(
        Query query, Class<T> collectionClass, boolean doFanOut) {

        if (query.getQueryMap().containsKey("_id")) {
            return findById(
                query.getQueryMap().get("_id").toString(),
                collectionClass,
                doFanOut
            );
        }

        queryCache.clear();
        return doFindOne(query, collectionClass, doFanOut);
    }

    public <T extends IsEntity> Long count(Query query, Class<T> type) {
        doCollectionClassValidation(type);
        Entity entity = type.getAnnotation(Entity.class);
        doQueryValidation(query, type);

        String entityName = entity.name();

        DBObject refObject =
            BasicDBObjectBuilder.start(query.getQueryMap()).get();

        return new Long(getDatabase()
            .getCollection(entityName).count(refObject));
    }

    protected <T extends IsEntity> T doFindOne(
        Query query, Class<T> collectionClass, boolean doFanOut
    ) {
        List<T> results = doQuery(query, collectionClass, doFanOut);
        if (results.size() > 0) {
            return results.get(0);
        } else {
            throw new NotFoundException();
        }
    }

    protected <T extends IsEntity> List<T> doQuery(
        Query query, Class<T> collectionClass, boolean doFanOut
    ) {

        doCollectionClassValidation(collectionClass);
        Entity collectionDb
            = collectionClass.getAnnotation(Entity.class);
        doQueryValidation(query, collectionClass);
        String collectionName = collectionDb.name();

        query = doRegexpQueryManipulation(query);

        DBObject referenceObject
            = BasicDBObjectBuilder.start(query.getQueryMap()).get();

        DBCursor cursor;

        if(query.getLimit() > 0) {
            cursor = getDatabase()
                .getCollection(collectionName)
                .find(referenceObject)
                .limit(query.getLimit());
        } else {
            cursor =
                getDatabase().getCollection(collectionName).find(referenceObject);
        }

        if (query.getSortMap().size() > 0) {
            LOG.fine(BasicDBObjectBuilder.start(query.getSortMap()).get().toString());
            cursor.sort(
                new BasicDBObject("intData", 1)
            );
        }


        List<T> results = new ArrayList<T>();

        while (cursor.hasNext()) {
            DBObject currentObject = cursor.next();
            ObjectId id = (ObjectId) currentObject.get("_id");

            if (queryCache.containsKey(id)) {
                LOG.fine("Getting from query cache.");
                results.add((T) queryCache.get(id));
            } else {
                T isDomainObject = buildDomainObject(
                    currentObject,
                    collectionClass,
                    doFanOut
                );

                results.add(isDomainObject);
            }
        }

        return results;

    }

    protected Query doRegexpQueryManipulation(Query query) {
        if (query.getRegularExpressionKeys().size() > 0) {
            for (String key : query.getRegularExpressionKeys()) {
                LOG.fine("Compiling regexp: " + query.getQueryMap().get(key));
                String regExpStr = (String) query.getQueryMap().get(key);
                query.getQueryMap().put(key, Pattern.compile(regExpStr));

                LOG.fine(BasicDBObjectBuilder.start(query.getQueryMap()).get().toString());
            }
        }

        return query;
    }

    protected void doQueryValidation(
        Query query,
        Class<? extends IsEntity> clazz) {
        List<String> columnNames = getColumnNames(clazz);
        columnNames.add("$or");

        for (String key : query.getQueryMap().keySet()) {
            if (!columnNames.contains(key)) {
                throw new QueryException(key, clazz.getName());
            }
        }
    }

    protected List<String> getColumnNames(Class clazz) {
        List<String> columnNames = new ArrayList<String>();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Column.class) && method.isAnnotationPresent(Embedded.class)) {
                String columnName = method.getAnnotation(Column.class).name();
                List<String> embeddedColumnNames = getColumnNames(method.getReturnType());
                for (String embeddedColumnName : embeddedColumnNames) {
                    columnNames.add(columnName + "." + embeddedColumnName);
                }
            } else if (method.isAnnotationPresent(Column.class)) {
                columnNames.add(
                    method.getAnnotation(Column.class).name()
                );
            }
        }
        //always allowed _id
        columnNames.add("_id");

        return columnNames;
    }

    protected void doCollectionClassValidation(Class clazz) {
        if (clazz == null || !clazz.isAnnotationPresent(Entity.class)) {

            if (clazz != null) {
                LOG.warning("INVALID COLLECTION: " + clazz.getSimpleName());
            }

            throw new InvalidEntity(
                clazz.getSimpleName()
            );
        }
    }

    protected <J extends IsSerializable> J buildDomainObject(
        DBObject recordToBuildFrom, Class<J> collectionClass, boolean doFanOut
    ) {

        //throw exception if this is not annotated with DbCollection
        if (!collectionClass.isAnnotationPresent(Entity.class)) {
            throw new InvalidEntity(collectionClass.getSimpleName());
        }

        Entity dbCollection = collectionClass.getAnnotation(Entity.class);
        J pojoToBuild;

        //Throw an error immediately if we can not get a new instance
        try {
            pojoToBuild = collectionClass.newInstance();
        } catch (Exception e) {
            throw new Error(e);
        }

        ObjectId idOfPojo = (ObjectId) recordToBuildFrom.get("_id");
        //cache this object so if we have any references, pointing to this
        //object they will grab the object reference from cache rather
        //than causing an infinite loop of recursion
        if (pojoToBuild instanceof IsEntity) {
            IsEntity pojoToBuildAsDomainObject =
                (IsEntity) pojoToBuild;
            queryCache.put(idOfPojo, pojoToBuildAsDomainObject);
            pojoToBuildAsDomainObject.setId(idOfPojo.toString());
            pojoToBuildAsDomainObject
                .setCreatedDatetime(new Date(idOfPojo.getTime()));
        }

        for (Method method : collectionClass.getMethods()) {

            Object valueToSet = null;
            Method setter = null;

            if (method.isAnnotationPresent(Reference.class)) {
                Reference reference = method.getAnnotation(Reference.class);
                setter = getMethodFromString(
                    method.getName().replace("get", "set"),
                    collectionClass
                );

                try {
                    valueToSet = getValueFromReference(
                        reference,
                        setter,
                        method,
                        recordToBuildFrom,
                        doFanOut
                    );
                } catch (Exception e) {
                    throw new Error(e);
                }

            } else if (method.isAnnotationPresent(Column.class)) {
                Column column = method.getAnnotation(Column.class);
                setter = getMethodFromString(
                    method.getName().replace("get", "set"),
                    collectionClass
                );


                valueToSet = recordToBuildFrom.get(column.name());

                if (method.isAnnotationPresent(Embedded.class)) {
                    valueToSet = buildDomainObject(
                        (BasicDBObject) valueToSet,
                        (Class<IsSerializable>) method.getReturnType(),
                        false
                    );
                } else if (method.isAnnotationPresent(Enumerated.class)) {
                    if (!method.getReturnType().isEnum()) {
                        throw new RuntimeException("Can not enumerate"
                            + " a column with enumerated unless it"
                            + "returns an enum.");
                    }

                    valueToSet = getEnumValueFromStoredValue(valueToSet, method);
                }
            }

            if (valueToSet != null && setter != null) {
                try {
                    setter.invoke(pojoToBuild, valueToSet);
                } catch (Exception e) {
                    LOG.warning("Error trying to invoke setter: "
                        + setter.getName() + " on object "
                        + pojoToBuild.getClass().getSimpleName()
                        + " with value " + valueToSet.toString()
                    );
                    throw new Error(e);
                }
            }
        }

        return pojoToBuild;
    }

    private Object getEnumValueFromStoredValue(Object value, Method getter) {
        Class enumClass = getter.getReturnType();
        Enumerated enumAnnotation
            = getter.getAnnotation(Enumerated.class);

        int ordinalCnt = 0;

        for (Object enumConstant: enumClass.getEnumConstants()) {
            Enum constantAsEnum = (Enum) enumConstant;

                if (constantAsEnum.name().equals(value)) {
                    return constantAsEnum;
                } /*else if (enumAnnotation.value().equals(EnumeratedType.ORDINAL)
                    && constantAsEnum.ordinal() == ordinalCnt++) {
                    return constantAsEnum;
                }
                   *
                   */

                ordinalCnt++;
        }

        throw new RuntimeException(
            "Did not find suitable enum value for "
            + value + " for enum " + enumClass.getSimpleName()
        );
    }

    private Object getValueFromReference(
        Reference reference,
        Method setter,
        Method getter,
        DBObject recordToBuildFrom,
        boolean doFanOut
    ) throws IllegalAccessException, InstantiationException, InvalidReference {
        if (reference.type().equals(ReferenceType.ONE_TO_ONE)) {

            Class referencedClass = getter.getReturnType();

            if (!referencedClass.isAnnotationPresent(Entity.class)) {
                throw new InvalidReference(
                    "Referenced class must be annotated with CollectionDb"
                );
            }
            if (!getter.isAnnotationPresent(Column.class)) {
                //TODO replace with its own exception
                throw new InvalidReference(
                    "One To One references must have a column annotation."
                );
            }

            Column column = getter.getAnnotation(Column.class);

            //we know this is a getter since only getters can
            //be annotated
            ObjectId referencedId = (ObjectId) recordToBuildFrom.get(column.name());

            //should we fan out and get all of the objects properties
            if (doFanOut) {
                Query findQuery = new Query().start("_id").is(referencedId);
                return doFindOne(findQuery, referencedClass, doFanOut);
            } else {
                //if not just return a IsDomainObject pojo with just the id set
                IsEntity referencedDomainObject =
                    (IsEntity) referencedClass.newInstance();
                referencedDomainObject.setId(referencedId.toString());
                return referencedDomainObject;
            }
        } else {
            /*
             * Since this is one to many we are dealing with some type
             * of collection. We need to find the generic type of the
             * collection
             */
            ParameterizedType parameterizedType
                = (ParameterizedType) getter.getGenericReturnType();
            Type[] type = parameterizedType.getActualTypeArguments();
            Class referenceType = (Class) type[0];
            ObjectId referencedId = (ObjectId) recordToBuildFrom.get("_id");
            Query findQuery = new Query()
                .start(reference.managedBy()).is(referencedId);
            return doQuery(findQuery, referenceType, doFanOut);
        }
    }

}
