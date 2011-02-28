/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.translate;


import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache.ColumnMeta;
import org.pjherring.mongogwt.server.guice.DatabaseModule.EntityList;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Enumerated;
import org.pjherring.mongogwt.shared.exception.InvalidReference;

/**
 *
 * @author pjherring
 */
public class DBObjectToPojo {

    private final static Logger LOG = Logger.getLogger(DBObjectToPojo.class.getSimpleName());

    protected List<Class<? extends IsEntity>> entityList;
    protected EntityMetaCache entityMetaCache;

    /*
     * This constructor takes in a list of the applications entities. It also
     * instantiates the cache map.
     *
     * @param entityList List of valid enties for application.
     */
    @Inject
    public DBObjectToPojo(
        @EntityList List<Class<? extends IsEntity>> entityList,
        EntityMetaCache entityMetaCache) {

        this.entityList = entityList;
        this.entityMetaCache = entityMetaCache;
    }

    /*
     * Translates a DBObject to an Entity pojo.
     *
     * @param dbObject The mongoDB DBObject to translate into a pojo.
     * @param type The type to translate the dbObject in to.
     *
     * @return The Entity pojo.
     */
    public <T extends IsStorable> T translate(DBObject dbObject, Class<T> type, boolean doFanOut) {

        T pojoToBuild;
        try {
            pojoToBuild = type.newInstance();
        } catch (Exception e) {
            LOG.warning("Exception while getting a new instance of: " + type.getName());
            throw new RuntimeException(e);
        }

        Set<ColumnMeta> columnMetaSet = entityMetaCache.getColumnMetaSet(type);

        for (ColumnMeta columnMeta : columnMetaSet) {
            Method setter = columnMeta.getSetter();
            Object value = dbObject.get(columnMeta.getColumnAnnotation().name());


            if (value != null) {

                //ONE TO MANY REFERENCE
                if (value instanceof BasicDBList) {
                    BasicDBList dbList = (BasicDBList) value;

                    if (dbList.size() > 0) {
                        if (dbList.get(0) instanceof DBRef) {
                            value = getOneToManyReference(dbList, setter, doFanOut);
                        } else {
                            Class collectionClass = Arrays.asList(setter.getParameterTypes()).get(0);
                            value = getCollection(dbList, setter, collectionClass);
                        }
                    }

                } else if (value.getClass().equals(DBRef.class)) /*many to one reference */ {
                    value = getPojoFromReference((DBRef) value, doFanOut);
                } else if (value instanceof DBObject) { /* embedded object */
                    DBObject embeddedObject = (DBObject) value;
                    value = translate(embeddedObject, (Class<T>) Arrays.asList(setter.getParameterTypes()).get(0), doFanOut);
                }

                //deal with enums
                if (columnMeta.getGetter().isAnnotationPresent(Enumerated.class)) {
                    value = getEnumValueFromString((String) value, (Class<Enum>) columnMeta.getGetter().getReturnType());
                }

                try {
                    setter.invoke(pojoToBuild, value);
                } catch (Exception e) {
                    LOG.warning("Exception while using setter: " + setter.getName() + " to set the value " + value.toString() + " on a entity " + type.getName());
                    throw new RuntimeException(e);
                }
            }
        }


        if (IsEntity.class.isAssignableFrom(type) && dbObject.containsField("_id")) {
            ObjectId id = (ObjectId) dbObject.get("_id");
            ((IsEntity) pojoToBuild).setId(id.toString());
            ((IsEntity) pojoToBuild).setCreatedDatetime(new Date(id.getTime()));
        }

        return pojoToBuild;
    }

    /*
     * This method actually translate the dbObject to the entity. It uses a
     * map which maps column names to the Entity's setters.
     *
     * @param dbObject DBObject to translate.
     * @param type The type to translate the DBObject to.
     * @param entityTranslationMap The map to use to translate the DBObject.
     *
     * @return The translated entity.
     */
    protected <T extends IsStorable, Q extends IsEntity, S> T translateWithCache(
        DBObject dbObject,
        Class<T> type,
        boolean doFanOut,
        Map<String, Method> entityTranslationMap) {


        T pojoToBuild;
        try {
            pojoToBuild = type.newInstance();
        } catch (Exception e) {
            LOG.warning("Exception while getting a new instance of: " + type.getName());
            throw new RuntimeException(e);
        }

        for (String columnName : entityTranslationMap.keySet()) {
            Method setter = entityTranslationMap.get(columnName);
            Object value = dbObject.get(columnName);


            if (value != null) {

                //ONE TO MANY REFERENCE
                if (value instanceof BasicDBList) {
                    BasicDBList dbList = (BasicDBList) value;
                    if (dbList.size() > 0) {
                        if (dbList.get(0) instanceof DBRef) {
                            value = getOneToManyReference(dbList, setter, doFanOut);
                        } else {
                            Class collectionClass = Arrays.asList(setter.getParameterTypes()).get(0);
                            value = getCollection(dbList, setter, collectionClass);
                        }
                    }
                } else if (value.getClass().equals(DBRef.class)) /*many to one reference */ {
                    value = getPojoFromReference((DBRef) value, doFanOut);
                } else if (value instanceof DBObject) { /* embedded object */
                    DBObject embeddedObject = (DBObject) value;
                    value = translate(embeddedObject, (Class<T>) Arrays.asList(setter.getParameterTypes()).get(0), doFanOut);
                } 

                try {
                    setter.invoke(pojoToBuild, value);
                } catch (Exception e) {
                    LOG.warning("Exception while using setter: " + setter.getName() + " to set the value " + value.toString() + " on a entity " + type.getName());
                    throw new RuntimeException(e);
                }
            }
        }


        if (IsEntity.class.isAssignableFrom(type) && dbObject.containsField("_id")) {
            ObjectId id = (ObjectId) dbObject.get("_id");
            ((IsEntity) pojoToBuild).setId(id.toString());
            ((IsEntity) pojoToBuild).setCreatedDatetime(new Date(id.getTime()));
        }

        return pojoToBuild;
    }

    /*
     * This methods takes in a genric Object and returns some sort of collection.
     * The Object should be some array of a type MongoDB understands, such as a
     * String, Integer, Long, Date.
     *
     * @param value The generic collection object
     * @param setter The method from the Entity we are building used to set
     *      the value.
     *
     * @return The collection
     */
    private <S> Object getCollection(BasicDBList dbList, Method setter, Class collectionClass) {

        List<S> list = (List<S>) dbList;

        if (Set.class.isAssignableFrom(collectionClass)) {
            return new HashSet<S>(list);
        } 

        return list;
    }

    /*
     * This will fetch some sort of collection for a one to many reference
     *
     * @param refs An array of DBRef that will be used to get the collection
     * @param setter The method of the Entity we are trying to build used
     *      as a setter.
     */
    private Collection<IsEntity> getOneToManyReference(
        BasicDBList refs,
        Method setter,
        boolean doFanOut) {

        if (refs.size() > 0) {
            List<IsEntity> referencedEntities = new ArrayList<IsEntity>();

            for (Object ref : refs) {
                referencedEntities.add(getPojoFromReference((DBRef) ref, doFanOut));
            }

            Class<? extends Collection> collectionType
                = (Class<? extends Collection>) Arrays.asList(setter.getParameterTypes()).get(0);

            if (Set.class.isAssignableFrom(collectionType)) {
                return new HashSet(referencedEntities);
            } else {
                return referencedEntities;
            }
        }

        return null;
    }

    /*
     * Get the Entity pojo that is reference in the Many To One Reference.
     *
     * @param ref The DBRef to use to fetch the pojo.
     * @return The entity that represents the DBRef's referenced DBObject
     */
    private <Q extends IsEntity> IsEntity getPojoFromReference(DBRef ref, boolean doFanOut) {
        Class<Q> entityType = (Class<Q>) getEntityClassFromDBRef(ref);

        DBObject refAsDBObject = ref.fetch();

        if (doFanOut) {
            return translate(refAsDBObject, entityType, doFanOut);
        } else {
            try {
                Q referencedEntity = entityType.newInstance();
                ObjectId id = (ObjectId) refAsDBObject.get("_id");
                referencedEntity.setId(id.toString());
                referencedEntity.setCreatedDatetime(new Date(id.getTime()));

                return referencedEntity;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Iterate through the list of valid entities to get the class of a DBRef.
     * This will throw an InvalidReference exception if the entity is not found
     * in the list.
     *
     * @param ref The DBRef to use to find the Entity
     * @return The class of the Entity
     * @throws InvalidReference
     */
    private Class<? extends IsEntity> getEntityClassFromDBRef(DBRef ref) {
        for (Class<? extends IsEntity> clazz : entityList) {
            if (clazz.getAnnotation(Entity.class).name().equals(ref.getRef())) {
                return (Class<? extends IsEntity>) clazz;
            }
        }

        throw new InvalidReference("Did you forget to add the entity type " + ref.getRef() + " to your entity list?");

    }

    private Object getEnumValueFromString(String value, Class<Enum> aClass) {

        for (Enum someEnum : aClass.getEnumConstants()) {
            if (someEnum.name().toUpperCase().equals(value.toUpperCase())) {
                return someEnum;
            }
        }

        //should never get here
        return null;
    }
}
