/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.translate;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache;
import org.pjherring.mongogwt.server.domain.cache.EntityMetaCache.ColumnMeta;
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
public class PojoToDBObject {

    protected final static Logger LOG = Logger.getLogger(PojoToDBObject.class.getSimpleName());
    protected DB mongoDb;
    protected EntityMetaCache entityMetaCache;

    @Inject
    public PojoToDBObject(DB mongoDb, EntityMetaCache entityMetaCache) {
        this.mongoDb = mongoDb;
        this.entityMetaCache = entityMetaCache;
    }

    /*
     * Translate an @code IsStorable pojo to a @code DBObject
     *
     * @param isStorable The pojo to translate
     *
     * @return the DBObject that represents the pojo
     */
    public <T extends IsStorable, S> DBObject translate(T isStorable) {
        DBObject toReturn = new BasicDBObject();
        Set<ColumnMeta> columnMetaSet = entityMetaCache.getColumnMetaSet(isStorable.getClass());

        //iterate through columnNames to store the data
        for (ColumnMeta columnMeta : columnMetaSet) {
            Method getter = columnMeta.getGetter();
            Object value = null;

            try {
                value = getter.invoke(isStorable);
            } catch (Exception e) {
                LOG.warning("Exception thrown while attempting to invoke getter: " + getter.getName() + " on " + isStorable.getClass());
                throw new RuntimeException(e);
            }

            if (value != null) {

                if (getter.isAnnotationPresent(Enumerated.class)
                    && value instanceof Enum) {

                    value = ((Enum) value).name().toUpperCase();

                } else if (getter.isAnnotationPresent(Embedded.class)
                    && value instanceof IsEmbeddable) {

                    value = (DBObject) translate((IsEmbeddable) value);

                } else if (getter.isAnnotationPresent(Reference.class)) {

                    Reference reference = getter.getAnnotation(Reference.class);
                    value = getValueFromReference(value, reference);

                } else if (value instanceof Collection) {
                    //we set all of this to be arrays, this is a type MongoDB understands
                    List<S> list = new ArrayList<S>((Collection<S>) value);
                    value = listToBasicDBList(list);
                } else if (value.getClass().isArray()) {
                    value = listToBasicDBList((List<S>) Arrays.asList((S[]) value));
                }

                toReturn.put(columnMeta.getColumnAnnotation().name(), value);
            }
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
     * This method takes a collection of a generic type in and transforms it intoo
     * an array. This will only work for collections with one or more items, which is
     * fine because we don't care about empty collections and don't need to store
     * them.
     *
     * @param c The collection to transform
     * @return the new array or null
     */
    private <T> BasicDBList listToBasicDBList(final List<T> list) {
        BasicDBList basicDBList = new BasicDBList();
        for (T item : list) {
            basicDBList.add(item);
        }
        return basicDBList;
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
        if (reference.type().equals(ReferenceType.ONE_TO_MANY)) {
            Iterable<? extends IsEntity> valueAsIterable
                = (Iterable<? extends IsEntity>) value;

            BasicDBList list = new BasicDBList();

            for (IsEntity referenceEntity : valueAsIterable) {
                if (referenceEntity.getId() == null) {
                    throw new InvalidReference("Can not reference an unpersisted entity.");
                }

                list.add(
                    new DBRef(
                        mongoDb,
                        referenceEntity.getClass().getAnnotation(Entity.class).name(),
                        new ObjectId(referenceEntity.getId())
                    )
                );
            }

            value = list;
        } else {
            IsEntity valueAsEntity = (IsEntity) value;

            if (valueAsEntity.getId() == null) {
                throw new InvalidReference("Can not reference an unpersisted entity.");
            }

            value = new DBRef(
                mongoDb,
                valueAsEntity.getClass().getAnnotation(Entity.class).name(),
                new ObjectId(valueAsEntity.getId())
            );

        }

        return value;
    }

}
