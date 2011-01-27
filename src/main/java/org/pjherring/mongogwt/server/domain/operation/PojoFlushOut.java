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
 * This class takes in an Entity and fetch's all of its references that the
 * entity does not manage.
 *
 * @author pjherring
 */
@Singleton
public class PojoFlushOut {

    /*
     * This is a simple pojo to carry data about a Reference
     */
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

    /*
     * Convenience method to clear the cache. This is mostly for testing, and
     * should not be called in the context of the application.
     */
    public void clearCache() {
        entityCacheMap.clear();
    }

    /*
     * The main method of this class. Takes in an entity and fetches its
     * non managed references.
     *
     * @param entity The entity to get the non-managed references for.
     * @param doFanOut Whether or not to grab all the attributes of the
     *      non managed references. If true it will, if false it will only
     *      grab the id and createdDateTime
     *
     * @return void
     */
    public void flush(IsEntity entity, boolean doFanOut) {
        Class<? extends IsEntity> clazz = entity.getClass();
        if (entityCacheMap.containsKey(clazz)) {
            flushWithReferenceList(entity, doFanOut, entityCacheMap.get(clazz));
        } else {
            entityCacheMap.put(clazz, getEntityReferences(clazz));
            flush(entity, doFanOut);
        }
    }

    /*
     * Uses the list of references to get the non managed references.
     *
     * @param entity The entity to get the non-managed references for.
     * @param doFanOut Whether or not to grab complete referenced objects.
     * @param references List of the references
     *
     * @return void
     */
    private void flushWithReferenceList(
        IsEntity entity,
        boolean doFanOut,
        List<ReferenceData> references) {

        Class<? extends IsEntity> clazz = entity.getClass();

        for (ReferenceData referenceData : references) {

            DBCursor cursor = getReferences(entity, doFanOut, referenceData);
            Object value = null;
            ReferenceType typeOfReference = referenceData.getReference().type();

            if (typeOfReference.equals(ReferenceType.ONE_TO_MANY)) {
                //fetch references
                List<IsEntity> entityList = new ArrayList<IsEntity>();

                while (cursor.hasNext()) {
                    entityList.add(translate.translate(
                        cursor.next(),
                        referenceData.getReferencedClass(),
                        doFanOut
                    ));
                }

                value = Set.class.isAssignableFrom(
                    (referenceData.getGetter().getReturnType())
                ) ? new HashSet(entityList) : entityList;
                    
            } else {
                /*
                 * Since this is a many to one or a one to one we know that
                 * the result of this query should only be one object.
                 */
                DBObject referenceAsObject = cursor.limit(1).next();

                value = translate.translate(
                    referenceAsObject,
                    referenceData.getReferencedClass(),
                    doFanOut
                );

            }

            try {
                referenceData.getSetter().invoke(entity, value);
            } catch (Exception e) {
                LOG.warning("Failure in setting: " 
                    + referenceData.getSetter().getName()
                    + " on " + clazz.getName()
                );

                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Queries the database to get the references.
     *
     * @param entity Entity you are setting the references on
     * @param doFanOut Do fetch the complete entities or just the id and createdDate
     * @param referenceData contains information about the reference
     *
     * @return the cursor for the DBObjects
     *
     */
    private DBCursor getReferences(
        IsEntity entity,
        boolean doFanOut,
        ReferenceData referenceData) {

        /*
         * If not fanning out we just want the id and nothing else about the
         * referenced entity.
         */
        DBObject keys = doFanOut ? null : new BasicDBObject("_id", 1);
        String entityCollectionName =
            entity.getClass().getAnnotation(Entity.class).name();
        DBObject query = new BasicDBObject(
            referenceData.getReference().managedBy(),
            new DBRef(mongoDb, entityCollectionName, new ObjectId(entity.getId()))
        );

        String referencedCollectionName = referenceData
            .getReferencedClass().getAnnotation(Entity.class).name();

        return mongoDb.getCollection(referencedCollectionName)
            .find(query, keys);
    }

    /*
     * This composes a list of ReferenceData for each entity. Should only run
     * once for each entity.
     *
     * @param clazz The class of the entity to compose list for.
     *
     * @return The list of ReferenceData
     */
    private List<ReferenceData> getEntityReferences(Class<? extends IsEntity> clazz) {

        List<ReferenceData> referenceData = new ArrayList<ReferenceData>();

        for (Method getter : clazz.getMethods()) {

            if (getter.isAnnotationPresent(Reference.class)) {

                Reference reference = getter.getAnnotation(Reference.class);

                /*
                 * Make sure this is a non managed entity
                 */
                if (!reference.managedBy().equals("")
                    && !getter.isAnnotationPresent(Column.class)) {

                    ReferenceData data = new ReferenceData();

                    data.setReference(reference);
                    data.setGetter(getter);

                    //get the Class of the entity referenced
                    if (reference.type().equals(ReferenceType.ONE_TO_MANY)) {
                        /*
                         * have to use the getter here to get the type of
                         * the entity referenced
                         */
                        ParameterizedType parameterizedType
                            = (ParameterizedType) getter.getGenericReturnType();
                        Type[] type =
                            parameterizedType.getActualTypeArguments();
                        Class referenceType = (Class) type[0];
                        data.setReferencedClass(referenceType);

                    } else {
                        data.setReferencedClass(
                            (Class<? extends IsEntity>) getter.getReturnType()
                        );
                    }

                    //get the setter
                    try {
                        data.setSetter(
                            clazz.getMethod(
                                "s" + getter.getName().substring(1),
                                getter.getReturnType()
                            )
                        );
                    } catch (Exception e) {
                        LOG.warning("Problems getting setter for: " 
                            + clazz.getName() + " off of getter: "
                            + getter.getName()
                        );
                        throw new RuntimeException(e);
                    }

                    referenceData.add(data);
                }
            }

        }

        return referenceData;
    }

}
