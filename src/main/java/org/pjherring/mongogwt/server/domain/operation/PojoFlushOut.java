/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.server.domain.operation;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
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
import org.pjherring.mongogwt.shared.IsEntity;
import org.pjherring.mongogwt.shared.annotations.Column;
import org.pjherring.mongogwt.shared.annotations.Entity;
import org.pjherring.mongogwt.shared.annotations.Reference;
import org.pjherring.mongogwt.shared.annotations.enums.ReferenceType;

/**
 *
 * @author pjherring
 */
@Singleton
public class PojoFlushOut {

    public static class ReferenceData {

        private Class<? extends IsEntity> referencedClass;
        private Reference reference;
        private Method getter;
        private Method setter;

        public Class<? extends IsEntity> getReferencedClass() {
            return referencedClass;
        }

        public void setReferencedClass(Class<? extends IsEntity> referencedClass) {
            this.referencedClass = referencedClass;
        }

        public Reference getReference() {
            return reference;
        }

        public void setReference(Reference reference) {
            this.reference = reference;
        }

        public Method getGetter() {
            return getter;
        }

        public void setGetter(Method getter) {
            this.getter = getter;
        }

        public Method getSetter() {
            return setter;
        }

        public void setSetter(Method setter) {
            this.setter = setter;
        }
    }

    private final static Logger LOG = Logger.getLogger(PojoFlushOut.class.getName());

    protected DB mongoDb;
    protected DBObjectToPojo translate;
    protected Map<Class<? extends IsEntity>, List<ReferenceData>> entityCacheMap
        = new HashMap<Class<? extends IsEntity>, List<ReferenceData>>();

    @Inject
    public PojoFlushOut(DB mongoDb, DBObjectToPojo translate) {
        this.mongoDb = mongoDb;
        this.translate = translate;
    }

    public void clearCache() {
        entityCacheMap.clear();
    }

    public <T extends IsEntity> void flush(T entity, Class<T> clazz, boolean doFanOut) {
        if (entityCacheMap.containsKey(clazz)) {
            flushWithMap(entity, clazz, doFanOut, entityCacheMap.get(clazz));
        } else {
            entityCacheMap.put(clazz, getEntityReferences(clazz));
            flush(entity, clazz, doFanOut);
        }
    }

    private <T extends IsEntity> void flushWithMap(T entity, Class<T> clazz, boolean doFanOut, List<ReferenceData> references) {

        for (ReferenceData referenceData : references) {
            if (referenceData.getReference().type().equals(ReferenceType.ONE_TO_MANY)) {
                //fetch references
                DBObject keys = doFanOut
                    ? null
                    : new BasicDBObject("_id", 1);
                DBCursor cursor = mongoDb
                    .getCollection(
                        referenceData.getReferencedClass()
                            .getAnnotation(Entity.class).name()
                    ).find(new BasicDBObject(
                        referenceData.getReference().managedBy(),
                        new DBRef(
                            mongoDb,
                            clazz.getAnnotation(Entity.class).name(),
                            new ObjectId(entity.getId())
                        )
                    ), keys);

                List<IsEntity> entityList = new ArrayList<IsEntity>();

                while (cursor.hasNext()) {
                    entityList.add(translate.translate(cursor.next(), referenceData.getReferencedClass()));
                }

                try {
                    if (Set.class.isAssignableFrom((referenceData.getGetter().getReturnType()))) {
                        referenceData.getSetter().invoke(entity, new HashSet(entityList));
                    } else {
                        referenceData.getSetter().invoke(entity, entityList);
                    }
                } catch (Exception e) {
                    LOG.warning("ONE TO MANY: Failure in setting: " + referenceData.getSetter().getName() + " on " + clazz.getName());
                    throw new RuntimeException(e);
                }
            } else {
                /*
                 * Since this is a many to one or a one to one we know that
                 * the result of this query should only be one object.
                 */
                DBObject keys = doFanOut
                    ? null
                    : new BasicDBObject("_id", 1);

                DBObject referenceAsObject = mongoDb
                    .getCollection(
                        referenceData.getReferencedClass()
                            .getAnnotation(Entity.class).name()
                    ).find(new BasicDBObject(
                        referenceData.getReference().managedBy(),
                        new DBRef(
                            mongoDb,
                            clazz.getAnnotation(Entity.class).name(),
                            new ObjectId(entity.getId())
                        )
                    ), keys).limit(1).iterator().next();

                try {
                    referenceData.getSetter().invoke(
                        entity,
                        translate.translate(
                            referenceAsObject,
                            referenceData.getReferencedClass()
                        )
                    );
                } catch (Exception e) {
                    LOG.warning("Failure in setting: " + referenceData.getSetter().getName() + " on " + clazz.getName());
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private List<ReferenceData> getEntityReferences(Class<? extends IsEntity> clazz) {

        List<ReferenceData> referenceData = new ArrayList<ReferenceData>();

        for (Method getter : clazz.getMethods()) {

            if (getter.isAnnotationPresent(Reference.class)) {
                Reference reference = getter.getAnnotation(Reference.class);

                if (!reference.managedBy().equals("")
                    && !getter.isAnnotationPresent(Column.class)) {

                    ReferenceData data = new ReferenceData();

                    data.setReference(reference);
                    data.setGetter(getter);

                    //get the Class of the entity referenced
                    if (reference.type().equals(ReferenceType.ONE_TO_MANY)) {
                        //have to use the getter here to get the type of the entity referenced
                        ParameterizedType parameterizedType
                            = (ParameterizedType) getter.getGenericReturnType();
                        Type[] type = parameterizedType.getActualTypeArguments();
                        Class referenceType = (Class) type[0];
                        data.setReferencedClass(referenceType);
                    } else {
                        data.setReferencedClass((Class<? extends IsEntity>) getter.getReturnType());
                    }

                    //get the setter
                    try {
                        data.setSetter(clazz.getMethod("s" + getter.getName().substring(1), getter.getReturnType()));
                    } catch (Exception e) {
                        LOG.warning("Problems getting setter for: " + clazz.getName() + " off of getter: " + getter.getName());
                        throw new RuntimeException(e);
                    }

                    referenceData.add(data);
                }
            }

        }

        return referenceData;
    }

}
