/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.guice.DatabaseModule.EntityList;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.exception.InvalidReference;

/**
 *
 * @author pjherring
 */
@Singleton
public class DBObjectToPojoImpl implements DBObjectToPojo {

    private final static Logger LOG = Logger.getLogger(DBObjectToPojoImpl.class.getName());

    private long cacheHits = 0;
    private long cacheMisses = 0;

    protected Map<Class<? extends IsStorable>, Map<String, Method>> cacheMap;
    protected List<Class<? extends IsEntity>> entityList;

    /*
     * This constructor takes in a list of the applications entities. It also
     * instantiates the cache map.
     *
     * @param entityList List of valid enties for application.
     */
    @Inject
    public DBObjectToPojoImpl(@EntityList List<Class<? extends IsEntity>> entityList) {
        cacheMap = new HashMap<Class<? extends IsStorable>, Map<String, Method>>();
        this.entityList = entityList;
    }

    /*
     * Translates a DBObject to an Entity pojo.
     *
     * @param dbObject The mongoDB DBObject to translate into a pojo.
     * @param type The type to translate the dbObject in to.
     *
     * @return The Entity pojo.
     */
    public <T extends IsStorable> T translate(DBObject dbObject, Class<T> type) {
        if (cacheMap.containsKey(type)) {
            cacheHits++;
            return translateWithCache(dbObject, type, cacheMap.get(type));
        } else {
            cacheMisses++;
            cacheMap.put(type, createEntityTranslationMap(dbObject, type));
            //do this to maintain correct count since we are using recursion
            cacheHits--;
            return translate(dbObject, type);
        }
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
                if (value.getClass().equals(DBRef[].class)) {
                    value = getOneToManyReference((DBRef[]) value, setter);
                } else if (value.getClass().equals(DBRef.class)) /*many to one reference */ {
                    value = getManyToOneReference((DBRef) value);
                } else if (value instanceof DBObject) { /* embedded object */
                    DBObject embeddedObject = (DBObject) value;
                    value = translate(embeddedObject, (Class<T>) Arrays.asList(setter.getParameterTypes()).get(0));
                } else if (value.getClass().isArray()) { /* collection of a basic type (String, int, Integer, etc...) */
                    Class collectionClass = Arrays.asList(setter.getParameterTypes()).get(0);
                    //if collection class is an array don't do any manipulation
                    if (!collectionClass.isArray()) {
                        value = getCollection(value, setter, collectionClass);
                    }
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
    private <S> Collection<S> getCollection(Object value, Method setter, Class collectionClass) {

        S[] valueAsArray = (S[]) value;
        List<S> valueAsList = Arrays.asList(valueAsArray);
        if (Set.class.isAssignableFrom(Arrays.asList(setter.getParameterTypes()).get(0))) {
            return new HashSet((List<S>) valueAsList);
        }

        return valueAsList;
    }

    /*
     * This will fetch some sort of collection for a one to many reference
     *
     * @param refs An array of DBRef that will be used to get the collection
     * @param setter The method of the Entity we are trying to build used
     *      as a setter.
     */
    private <Q extends IsEntity> Collection<Q> getOneToManyReference(DBRef[] refs, Method setter) {

        if (refs.length > 0) {
            Class<Q> entityType = (Class<Q>) getEntityFromDBRef(refs[0]);

            List<Q> referencedEntities = new ArrayList<Q>();

            for (DBRef ref : refs) {
                DBObject refAsObject = ref.fetch();
                referencedEntities.add(translate(refAsObject, entityType));
            }

            Class<? extends Collection> collectionType
                = (Class<? extends Collection>) Arrays.asList(setter.getParameterTypes()).get(0);

            if (Set.class.isAssignableFrom(collectionType)) {
                return new HashSet<Q>(referencedEntities);
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
    private <Q extends IsEntity> IsEntity getManyToOneReference(DBRef ref) {
        Class<Q> entityType = (Class<Q>) getEntityFromDBRef(ref);
        DBObject refAsDBObject = ref.fetch();
        return translate(refAsDBObject, entityType);
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
    private Class<? extends IsEntity> getEntityFromDBRef(DBRef ref) {
        for (Class<? extends IsEntity> clazz : entityList) {
            if (clazz.getAnnotation(Entity.class).name().equals(ref.getRef())) {
                return (Class<? extends IsEntity>) clazz;
            }
        }

        throw new InvalidReference("Did you forget to add the entity type " + ref.getRef() + " to your entity list?");

    }

    /*
     * Takes a dbObject and creates a map with column names pointing to setters
     *
     * @param dbObject The dbObject to use to create the map.
     * @param The type of the pojo we are creating the map for
     *
     * @return the map
     */
    protected <T extends IsStorable> Map<String, Method> createEntityTranslationMap(
        DBObject dBObject,
        Class<T> type) {
        Map<String, Method> entityTranslationMap =
            new HashMap<String, Method>();

        for (Method getter : type.getMethods()) {
            if (getter.isAnnotationPresent(Column.class)) {

                Column column = getter.getAnnotation(Column.class);

                if (dBObject.containsField(column.name())) {
                    /*
                     * We want the setter.
                     */
                    try {
                        Method setter = type.getMethod(
                            "s" + getter.getName().substring(1),
                            getter.getReturnType()
                        );
                        entityTranslationMap.put(column.name(), setter);
                    } catch (Exception e) {
                        LOG.warning("Error while trying to get setter from: " + getter.getName() + " on class: " + type.getName());
                        throw new RuntimeException(e);
                    }
                }
            } 
        }

        return entityTranslationMap;
    }

    public long getCacheHits() {
        return cacheHits;
    }

    public long getCacheMisses() {
        return cacheMisses;
    }

    public void resetCache() {
        cacheHits = 0;
        cacheMisses = 0;
        cacheMap.clear();
    }

}
