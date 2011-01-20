/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.shared.IsEmbeddable;
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.IsStorable;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Embedded;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Enumerated;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;
import org.pjherring.mongogwt.shared.exception.InvalidReference;

/**
 *
 * This is a class that translates a pojo into a DBObject, which will be ready
 * for inserting into the mongoDb. It uses caching in the sense that it creates
 * a translation map for each type of entity in your application.
 *
 * @author pjherring
 * @since January 20th 2011
 */
@Singleton
public class PojoToDBObjectImpl implements PojoToDBObject {

    protected final static Logger LOG = Logger.getLogger(PojoToDBObjectImpl.class.getSimpleName());
    protected Map<Class<? extends IsStorable>, Map<String, Method>> translationMap;
    protected long cacheMapHits = 0L;
    protected long cacheMapMisses = 0L;
    protected DB mongoDb;

    public long getCacheHitCount() {
        return cacheMapHits;
    }

    public long getCacheMissCount() {
        return cacheMapMisses;
    }

    @Inject
    public PojoToDBObjectImpl(DB mongoDb) {
        translationMap = new HashMap<Class<? extends IsStorable>, Map<String, Method>>();
        this.mongoDb = mongoDb;
    }

    /*
     * Translate an @code IsStorable pojo to a @code DBObject
     *
     * @param isStorable The pojo to translate
     *
     * @return the DBObject that represents the pojo
     */
    public <T extends IsStorable> DBObject translate(T isStorable) {

        //do we already know the translation pattern?
        if (translationMap.containsKey(isStorable.getClass())) {
            cacheMapHits++;
            Map<String, Method> entityTranslationMap = translationMap.get(isStorable.getClass());
            return translateFromCache(isStorable, entityTranslationMap);
        } else {
            //we have to construct the translation map
            cacheMapMisses++;
            translationMap.put(isStorable.getClass(), createTranslationMap(isStorable));
            /*
             * We decrement here because we are about to use recursion. When
             * a hit is found the cacheMapHits is incremented, but in this case
             * it would be wrong to do so because this was indeed a miss.
             * Therefore, decrement to cancel out the increment (-1 + 1 = 0).
             */
            cacheMapHits--;
            return translate(isStorable);
        }
    }

    /*
     * This method uses a translation map to translate a IsStorable into a
     * DBObject.
     *
     * @param isStorable The pojo to transform.
     * @param entityTranslationMap The translation map to use for transformation.
     */
    private <T extends IsStorable> DBObject translateFromCache(
        T isStorable,
        Map<String, Method> entityTranslationMap) {

        DBObject toReturn = new BasicDBObject();

        //iterate through columnNames to store the data
        for (String columnName : entityTranslationMap.keySet()) {
            Method getter = entityTranslationMap.get(columnName);
            Object value = null;

            try {
                value = getter.invoke(isStorable);
            } catch (Exception e) {
                LOG.warning("Exception thrown while attempting to invoke getter: " + getter.getName() + " on " + isStorable.getClass());
                throw new RuntimeException(e);
            }

            if (getter.isAnnotationPresent(Enumerated.class)
                && value instanceof Enum) {

                value = ((Enum) value).name().toUpperCase();

            } else if (getter.isAnnotationPresent(Embedded.class)
                && value instanceof IsEmbeddable) {

                value = (DBObject) translate((IsEmbeddable) value);
            } else if (getter.isAnnotationPresent(Reference.class)) {

                Reference reference = getter.getAnnotation(Reference.class);
                value = getValueFromReference(value, reference);
            }

            toReturn.put(columnName, value);
        }

        if (isStorable instanceof IsEntity) {
            IsEntity isEntity = (IsEntity) isStorable;

            if (isEntity.getId() != null) {
                toReturn.put("_id", new ObjectId(isEntity.getId()));
            }
        }

        return toReturn;
    }

    /*
     * Actually creates the translation map. In essence, this method links each
     * column (represented by a Column Annotation) with a Getter. The output
     * is a map with a String (column name) pointing to a Method (the getter).
     * 
     * @param isStorable the Object we are creating a translation map for.
     * 
     * @return the translation map
     */
    private <T extends IsStorable> Map<String, Method> createTranslationMap(T isStorable) {
        Map<String, Method> entityTranslationMap =
            new HashMap<String, Method>();

        for (Method method : isStorable.getClass().getMethods()) {
            /*
             * We want this method if its a getter meaning no parameters
             * and annotated with column.
             */
            if (method.isAnnotationPresent(Column.class) && method.getParameterTypes().length == 0) {
                Column column = method.getAnnotation(Column.class);
                entityTranslationMap.put(column.name(), method);
            }
        }

        return entityTranslationMap;
    }

    /*
     * Will grab either an array of DBRef's or a DBRef to represent the reference
     * 
     * @param value The value fetched from isStorable
     * @param reference The Reference annotation
     * 
     * @return Either DBRef[] or DBRef
     */
    private Object getValueFromReference(Object value, Reference reference) {
        if (reference.type().equals(ReferenceType.MANY_TO_ONE)) {
            IsEntity valueAsEntity = (IsEntity) value;

            if (valueAsEntity.getId() == null) {
                throw new InvalidReference("Can not reference an unpersisted entity.");
            }

            value = new DBRef(
                mongoDb,
                valueAsEntity.getClass().getAnnotation(Entity.class).name(),
                new ObjectId(valueAsEntity.getId())
            );
        } else {

            Iterable<? extends IsEntity> valueAsIterable
                = (Iterable<? extends IsEntity>) value;

            List<DBRef> dbReferences = new ArrayList<DBRef>();

            for (IsEntity referenceEntity : valueAsIterable) {
                if (referenceEntity.getId() == null) {
                    throw new InvalidReference("Can not reference an unpersisted entity.");
                }

                dbReferences.add(
                    new DBRef(
                        mongoDb,
                        referenceEntity.getClass().getAnnotation(Entity.class).name(),
                        new ObjectId(referenceEntity.getId())
                    )
                );
            }

            value = dbReferences.toArray(new DBRef[dbReferences.size()]);
        }

        return value;
    }

    /*
     * Resets the cache. Clears the translation map and resets the counters.
     */
    public void resetCahce() {
        translationMap.clear();
        cacheMapHits = 0L;
        cacheMapMisses = 0L;
    }

}
