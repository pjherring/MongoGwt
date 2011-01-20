/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.util.StringUtil;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Enumerated;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.InvalidCollectionException;
import org.pjherring.mongogwt.shared.exception.InvalidReference;

/**
 *
 * @author pjherring
 */
@Singleton
public class PojoMapTranslatorOld {

    private final static Logger LOG = Logger.getLogger(PojoMapTranslatorOld.class.getName());

    private Map<Class<? extends IsStorable>, Map<String, Method>> pojoToDBObjectCache
        = new HashMap<Class<? extends IsStorable>, Map<String, Method>>();
    private Map<Class<? extends IsStorable>, Map<String, Method>> dbObjectToPojoCache
        = new HashMap<Class<? extends IsStorable>, Map<String, Method>>();

    private long dBObjectToPojoCacheHits = 0;
    private long dBObjectToPojoCacheMisses = 0;

    private long pojoToDBObjectCacheHits = 0;
    private long pojoToDBObjectCacheMisses = 0;

    private Map<ObjectId, IsEntity> domainObjectCache
        = new HashMap<ObjectId, IsEntity>();

    private final StringUtil stringUtil;
    private final DB mongoDb;

    @Inject
    public PojoMapTranslatorOld(StringUtil stringUtril, DB mongoDb) {
        this.stringUtil = stringUtril;
        this.mongoDb = mongoDb;
    }


    /*
     * Translate a DBObject to a Pojo
     */
    public <T extends IsEntity> T DBObjToPojo(
        DBObject dbObject,
        Class<T> clazz,
        boolean doFanOut) {
        domainObjectCache.clear();
        return doDBToPojoTranslation(dbObject, clazz, doFanOut);
    }

    public long getPojoToMapCacheHits() {
        return dBObjectToPojoCacheHits;
    }

    public long getPojoToMapCacheMisses() {
        return dBObjectToPojoCacheMisses;
    }

    public long getPojoToDBObjectCacheHits() {
        return dBObjectToPojoCacheHits;
    }

    public long getPojoToDBObjectCacheMisses() {
        return dBObjectToPojoCacheMisses;
    }

    /*
     * This method will take in a pojo and transform it into a @code DBObject .
     *
     * @param pojo The object to be transformed into a @code DBObject. This can
     * either be an Entity or a Embedded Object in an entity.
     *
     * @return The DBObject containing all the data from the pojo
     */
    public DBObject pojoToDBObject(IsStorable pojo) {

        //is the way to extract data from the pojo cached?
        if (pojoToDBObjectCache.containsKey(pojo.getClass())) {
            //increment cache hits
            pojoToDBObjectCacheHits++;

            /*
             * We first construct a map of key value pairs. Then MongoDB's
             * java driver transforms this map using @code BasicDBObjectBuilder
             * into a @code DBObject
             */
            Map<String, Object> dbObjectMap = new HashMap<String, Object>();

            Map<String, Method> columnNameGetter
                = pojoToDBObjectCache.get(pojo.getClass());

            for (String columnName : columnNameGetter.keySet()) {
                Method getter = columnNameGetter.get(columnName);
                Object value = null;

                //is this a reference
                if (getter.isAnnotationPresent(Reference.class)) {
                    //we can make the cast here because reference's should only
                    //be to other entities
                    LOG.info(getter.getName());
                    value = getReferencePojoToDBObject(getter, pojo);

                } else {
                    try {
                        //since this is a getter it requires no args
                        value = getter.invoke(pojo);
                    } catch (Exception e) {
                        LOG.warning("Error in invoking getter for column: "
                            + columnName + " on " + pojo.getClass().getName());
                        throw new RuntimeException(e);
                    }

                    if (getter.isAnnotationPresent(Enumerated.class)) {
                        value = ((Enum) value).name();
                    }

                    if (value instanceof IsEmbeddable) {
                        value = pojoToDBObject((IsStorable) value);
                    }
                }

                dbObjectMap.put(columnName, value);
            }

            if (pojo instanceof IsEntity) {
                dbObjectMap.put("_id", ((IsEntity) pojo).getId());
            }

            DBObject builtDBOBject = BasicDBObjectBuilder.start(dbObjectMap).get();

            return builtDBOBject;
        } else {
            pojoToDBObjectCacheMisses--;

            //otherwise we need to construct the map of column names to getters
            Map<String, Method> columnNameGetters = new HashMap<String, Method>();

            for (Method getter : pojo.getClass().getMethods()) {
                if (getter.isAnnotationPresent(Column.class)) {
                    columnNameGetters.put(
                        getter.getAnnotation(Column.class).name(),
                        getter
                    );
                }
            }

            pojoToDBObjectCache.put(pojo.getClass(), columnNameGetters);
            /*
             * since we just missed the hits will be falsely incremented
             * in this recursive call, decrement before call.
             */
            pojoToDBObjectCacheHits--;
            return pojoToDBObject(pojo);
        }

    }

    /*
     * This method will get the value to store in the DB. For these references
     * we do not need to store the whole object but mereley the id of the object 
     * @code ObjectID
     *
     * @param getter The method used to get the value of an attribute of an
     * Entity
     * @param pojo The Entity being examined.
     *
     * @return Object (Either a ObjectID or ObjectID[])
     */
    private Object getReferencePojoToDBObject(Method getter, IsStorable pojo) {
        Reference reference = getter.getAnnotation(Reference.class);


        Object value;
            try {
                value = getter.invoke(pojo);
            } catch (Exception e) {
                LOG.warning("Exception while invoking getter for a one to"
                    + " one reference for class: " + pojo.getClass().getName()
                    + " with getter: " + getter.getName());
                throw new RuntimeException(e);
            }

        if (reference.type().equals(ReferenceType.ONE_TO_ONE)) {
            String entityName
                = getter.getReturnType().getAnnotation(Entity.class).name();

                if (!(value instanceof IsEntity)) {
                    throw new InvalidReference();
                }

                IsEntity valueEntity = (IsEntity) value;

                if (valueEntity.getId() == null) {
                    throw new InvalidReference("You can not refer to an unpersisted Entity.");
                }

                return new DBRef(
                    mongoDb,
                    entityName,
                    new ObjectId(valueEntity.getId())
                );
        } else {
            Class collectionType = getter.getReturnType();
            ParameterizedType parameterizedType
                = (ParameterizedType) getter.getGenericReturnType();
            Type[] type = parameterizedType.getActualTypeArguments();
            //This is the @code Class of our entity
            Class<IsEntity> returnType = (Class<IsEntity>) type[0];
            String entityName = returnType.getAnnotation(Entity.class).name();
            if (!(value instanceof Set || value instanceof List)) {
                throw new InvalidReference("For a many to one or one to many "
                    + "reference the type of the column must be of type Iterable");
            }

            Iterable<IsEntity> collection = (Iterable) value;
            List<DBRef> dbRefs = new ArrayList<DBRef>();

            for (IsEntity referencedDomainObject : collection) {
                if (referencedDomainObject.getId() == null) {
                    throw new InvalidReference("You can not refer to an unpersisted Entity.");
                }

                DBRef ref = new DBRef(
                    mongoDb,
                    entityName,
                    new ObjectId(referencedDomainObject.getId())
                );
                dbRefs.add(ref);
            }

            return dbRefs.toArray(new DBRef[dbRefs.size()]);
        }
    }

    private <T extends IsStorable> T doDBToPojoTranslation(
        DBObject dbObject,
        Class<T> clazz,
        boolean doFanOut) {

        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new InvalidCollectionException(clazz.getName());
        }

        T pojoToBuild = null;
        ObjectId id = null;

        //this dbObject may not have an id as it may be an embedded entity
        if (IsEntity.class.isAssignableFrom(clazz)) {
            id = (ObjectId) dbObject.get("_id");

        //have we already constructed this object?
            if (domainObjectCache.containsKey(id)) {
                return (T) domainObjectCache.get(id);
            }

        }

        try {
            pojoToBuild = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*
         * Again, this dbObject may not be a top level entity, with an id.
         * Therefore we need to check here.
         */
        if (id != null) {
            ((IsEntity) pojoToBuild).setId(id.toString());
            ((IsEntity) pojoToBuild).setCreatedDatetime(new Date(id.getTime()));
        }

        //is this mapping cached?
        if (dbObjectToPojoCache.containsKey(clazz)) {
            //update the cache hit
            dBObjectToPojoCacheHits++;

            Map<String, Method> columnMethodMap = dbObjectToPojoCache.get(clazz);

            for (String columnName : columnMethodMap.keySet()) {
                Method getter = columnMethodMap.get(columnName);
                Method setter = null;
                try {
                    setter = getSetterFromGetter(getter, pojoToBuild.getClass());
                } catch (Exception e) {
                    LOG.warning("Exception while trying to get 'getter' from 'setter' for "
                        + getter.getName() + " on " + clazz.getName());
                    throw new RuntimeException(e);
                }

                Object valueToSet;
                if (getter.isAnnotationPresent(Reference.class)) {
                    valueToSet = getReferenceValueForPojo(
                        getter.getAnnotation(Reference.class),
                        dbObject,
                        doFanOut,
                        columnName,
                        getter
                    );
                } else {
                    //no reference
                    valueToSet = dbObject.get(columnName);


                    if (getter.isAnnotationPresent(Embedded.class)) {
                        valueToSet = doDBToPojoTranslation(
                            (DBObject) valueToSet,
                            (Class<IsStorable>) getter.getReturnType(),
                            doFanOut
                        );
                    } else if (getter.isAnnotationPresent(Enumerated.class)) {
                        valueToSet = getEnumFromValue(
                            (String) valueToSet,
                            (Class<Enum>) getter.getReturnType()
                        );
                    } 
                }

                try {
                    setter.setAccessible(true);
                    setter.invoke(pojoToBuild, valueToSet);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if (clazz.isAssignableFrom(IsEntity.class)) {
                domainObjectCache.put(id, (IsEntity) pojoToBuild);
            }

            return pojoToBuild;
        } else {
            dBObjectToPojoCacheMisses++;
            Map<String, Method> columnMethodMap = new HashMap<String, Method>();

            for (Method getter : clazz.getMethods()) {

                if (getter.isAnnotationPresent(Column.class)) {
                    Column column = getter.getAnnotation(Column.class);
                    String columnName = "";
                    //setter will never be null if we reach this point
                    if (IsEmbeddable.class.isAssignableFrom(clazz)) {
                        columnName = clazz.getAnnotation(Entity.class).name() + ".";
                    }

                    columnName+= column.name();

                    columnMethodMap.put(column.name(), getter);
                }
            }
            dbObjectToPojoCache.put(clazz, columnMethodMap);
            /*
             * We decrement the hits here because upon calling @code doDBToPojoTranslation
             * it will increment the counter, but this was a miss and should not be
             * counted as a hit.
             */
            dBObjectToPojoCacheHits--;
            /*
             * We've constructed our 'EntityCache' so use @code doDBToPojoTranslation
             * again to actually construct the method
             */
            return doDBToPojoTranslation(dbObject, clazz, doFanOut);

        }

    }

    /*
     * Gets the value of a reference. This could either be a @code Collection
     * of some Entities or a single @code Entity, depending on the relationship
     */
    private Object getReferenceValueForPojo(
        Reference reference,
        DBObject buildingFrom,
        boolean doFanOut,
        String columnName,
        Method getter) {


        //Are we dealing with a one to one reference
        LOG.info(columnName);
        if (reference.type().equals(ReferenceType.ONE_TO_ONE)) {
            //The return type here matches the type of the Entity
            Class referenceClass = getter.getReturnType();

            if (!referenceClass.isAnnotationPresent(Entity.class)) {
                throw new InvalidReference(referenceClass.getName());
            }

            DBRef ref = (DBRef) buildingFrom.get(columnName);
            Entity entity = (Entity) referenceClass.getAnnotation(Entity.class);

            if (doFanOut) {
                DBObject referencedDBObject = ref.fetch();
                //recursion here
                return doDBToPojoTranslation(referencedDBObject, referenceClass, doFanOut);
            } else {
                return doNoFanOutReference((ObjectId) ref.getId(), referenceClass);
            }
        } else {
            /*
             * The return type here is a Collection of some sort. We need to
             * find out the parameterized type
             */
            Class collectionType = getter.getReturnType();
            ParameterizedType parameterizedType
                = (ParameterizedType) getter.getGenericReturnType();
            Type[] type = parameterizedType.getActualTypeArguments();
            //This is the @code Class of our entity
            Class referenceType = (Class) type[0];

            List list = new ArrayList();

            DBRef[] referencedObjects = (DBRef[]) buildingFrom.get(columnName);
            /*
             * Loop through the refenced objects to translate them into partial
             * Entity (id and createdDatetime only) or full fanned out entity's
             */
            for (DBRef ref : referencedObjects) {
                if (doFanOut) {

                    if (!referenceType.isAnnotationPresent(Entity.class)) {
                        throw new InvalidReference(referenceType.getName());
                    }

                    Entity entity = (Entity) referenceType.getAnnotation(Entity.class);
                    list.add(doDBToPojoTranslation(ref.fetch(), referenceType, doFanOut));
                } else {
                    list.add(doNoFanOutReference((ObjectId) ref.getId(), referenceType));
                }
            }

            if (Set.class.isAssignableFrom(collectionType)) {
                Set set = new HashSet(list);
                return set;
            }

            return list;
        }
    }

    /*
     * This method simply puts and id and a createdDatetime into a @code
     * IsDomainObject
     *
     * @param ref The DBRef reference object
     * @param referenceClass The type of the @code Entity referenced
     */
    private <T extends IsEntity> T doNoFanOutReference(ObjectId id, Class<T> referenceClass) {
        try {
            T pojoToBuild = referenceClass.newInstance();
            pojoToBuild.setId(id.toString());
            pojoToBuild.setCreatedDatetime(new Date(id.getTime()));

            return pojoToBuild;
        } catch (Exception e) {
            LOG.warning("Exception thrown when getting partial reference for: "
                + referenceClass.getName());
            throw new RuntimeException(e);
        }
    }

    private Method getSetterFromGetter(Method getter, Class clazz)
        throws NoSuchMethodException, SecurityException {
        String getterName = getter.getName();
        String setterName = "s" + getterName.substring(1);

        return clazz.getMethod(setterName, getter.getReturnType());
    }

    private Enum getEnumFromValue(String valueToSet, Class<Enum> enumClass) {

        for (Enum enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().toUpperCase().equals(valueToSet.toUpperCase())) {
                return enumConstant;
            }
        }

        throw new RuntimeException("No Enum found from value: " + valueToSet);
    }

}
