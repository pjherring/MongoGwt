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

/**
 *
 * @author pjherring
 */
@Singleton
public class DBObjectToPojoImpl implements DBObjectToPojo {

    private final static Logger LOG = Logger.getLogger(DBObjectToPojoImpl.class.getName());
    protected Map<Class<? extends IsStorable>, Map<String, Method>> cacheMap;
    protected List<Class<? extends IsEntity>> entityList;

    @Inject
    public DBObjectToPojoImpl(@EntityList List<Class<? extends IsEntity>> entityList) {
        cacheMap = new HashMap<Class<? extends IsStorable>, Map<String, Method>>();
        this.entityList = entityList;
    }

    public <T extends IsStorable> T translate(DBObject dbObject, Class<T> type) {
        if (cacheMap.containsKey(type)) {
            return translateWithCache(dbObject, type, cacheMap.get(type));
        } else {
            cacheMap.put(type, createEntityTranslationMap(dbObject, type));
            return translate(dbObject, type);
        }
    }

    protected <T extends IsStorable, Q extends IsEntity> T translateWithCache(
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

            //ONE TO MANY REFERENCE
            if (value.getClass().equals(DBRef[].class)) {
                //need to find type of reference
                DBRef[] dbRefs = (DBRef[]) value;

                if (dbRefs.length > 0) {
                    String entityDbName = dbRefs[0].getRef();
                    Class<Q> entityType = null;

                    for (Class<? extends IsEntity> clazz : entityList) {
                        if (entityDbName.equals(clazz.getAnnotation(Entity.class).name())) {
                            entityType = (Class<Q>) clazz;
                            break;
                        }
                    }

                    List<Q> referencedEntities =
                        new ArrayList<Q>();

                    for (DBRef ref : dbRefs) {
                        DBObject refAsObject = ref.fetch();
                        referencedEntities.add(translate(refAsObject, entityType));
                    }

                    Class<? extends Collection> collectionType
                        = (Class<? extends Collection>) Arrays.asList(setter.getParameterTypes()).get(0);

                    if (Set.class.isAssignableFrom(collectionType)) {
                        value = new HashSet<Q>(referencedEntities);
                    } else {
                        value = referencedEntities;
                    }
                }


            } else if (value.getClass().equals(DBRef.class)) /*many to one reference */ {
                DBRef ref = (DBRef) value;

                Class<Q> entityType = null;
                for (Class<? extends IsEntity> clazz : entityList) {
                    if (clazz.getAnnotation(Entity.class).name().equals(ref.getRef())) {
                        entityType = (Class<Q>) clazz;
                        break;
                    }
                }

                DBObject refAsDBObject = ref.fetch();
                value = translate(refAsDBObject, entityType);
            }


            try {
                setter.invoke(pojoToBuild, value);
            } catch (Exception e) {
                LOG.warning("Exception while using setter: " + setter.getName() + " to set the value " + value.toString() + " on a entity " + type.getName());
                throw new RuntimeException(e);
            }
        }

        if (IsEntity.class.isAssignableFrom(type) && dbObject.containsField("_id")) {
            ObjectId id = (ObjectId) dbObject.get("_id");
            ((IsEntity) pojoToBuild).setId(id.toString());
            ((IsEntity) pojoToBuild).setCreatedDatetime(new Date(id.getTime()));
        }

        return pojoToBuild;
    }

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getCacheMisses() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetCache() {
        cacheMap.clear();
    }

}
